package nnu.wyz.systemMS.websocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.*;

@ServerEndpoint("/webSocket/execute/local")
@Component
public class LocalExecuteWebSocketServer {

    private static final ConcurrentHashMap<String, Session> frontendSessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Session> localPythonSessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<String, ScheduledFuture<?>> heartbeatTasks = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("[INFO] 前端 WebSocket 连接建立: " + session.getId());
        frontendSessions.put(session.getId(), session);

        // 启动心跳
        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
            try {
                if (session.isOpen()) {
                    JsonObject ping = new JsonObject();
                    ping.addProperty("type", "heartbeat");
                    ping.addProperty("timestamp", System.currentTimeMillis());
                    session.getBasicRemote().sendText(ping.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 10, 10, TimeUnit.SECONDS);
        heartbeatTasks.put(session.getId(), task);
    }

    @OnMessage
    public void onMessage(String message, Session frontendSession) {
        try {
            JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
            System.out.println("[RECEIVED] 前端请求：" + jsonMessage);

            // 若是中断请求，转发给 Python 执行服务
            if (jsonMessage.has("interrupt") && jsonMessage.get("interrupt").getAsBoolean()) {
                Session localSession = localPythonSessions.get(frontendSession.getId());
                if (localSession != null && localSession.isOpen()) {
                    localSession.getBasicRemote().sendText(jsonMessage.toString());
                    System.out.println("[INFO] 已向 Python 服务发送中断指令");
                } else {
                    System.err.println("[WARN] 无法找到对应的 Python 执行会话");
                }
                return;
            }

            // 建立连接并转发执行请求
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            URI uri = new URI("ws://127.0.0.1:8000/ws/run"); // 本地 FastAPI WebSocket

            container.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session localSession, EndpointConfig config) {
                    System.out.println("[INFO] 已连接到本地 Python 执行服务");

                    localPythonSessions.put(frontendSession.getId(), localSession);

                    try {
                        localSession.getBasicRemote().sendText(jsonMessage.toString());
                        System.out.println("[INFO] 已将文件执行请求发送到 FastAPI 服务");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    localSession.addMessageHandler((MessageHandler.Whole<String>) msg -> {
                        try {
                            frontendSession.getBasicRemote().sendText(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onError(Session session, Throwable thr) {
                    System.err.println("[ERROR] 本地执行服务连接错误");
                    thr.printStackTrace();
                }

                @Override
                public void onClose(Session session, CloseReason closeReason) {
                    System.out.println("[INFO] 本地执行服务连接关闭: " + closeReason);
                }
            }, uri);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                frontendSession.getBasicRemote().sendText("后端内部错误：" + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("[INFO] 前端 WebSocket 关闭: " + session.getId());
        frontendSessions.remove(session.getId());

        Session localSession = localPythonSessions.remove(session.getId());
        if (localSession != null && localSession.isOpen()) {
            try {
                localSession.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ScheduledFuture<?> task = heartbeatTasks.remove(session.getId());
        if (task != null) {
            task.cancel(true);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("[ERROR] 前端 WebSocket 异常: " + session.getId());
        throwable.printStackTrace();
    }
}
