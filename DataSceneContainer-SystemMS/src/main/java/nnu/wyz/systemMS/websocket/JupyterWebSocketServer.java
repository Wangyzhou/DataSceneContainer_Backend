package nnu.wyz.systemMS.websocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/webSocket/execute")
@Component
public class JupyterWebSocketServer {

    private static final ConcurrentHashMap<String, Session> frontendSessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Session> jupyterSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionKernelMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("[INFO] 前端 WebSocket 连接已建立: " + session.getId());
        frontendSessions.put(session.getId(), session);
    }

    @OnMessage
    public void onMessage(String message, Session frontendSession) {
        try {
            JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
            System.out.println("[RECEIVED] 前端消息: " + jsonMessage);

            if (jsonMessage.has("kernelId")) {
                String kernelId = jsonMessage.get("kernelId").getAsString();
                sessionKernelMap.put(frontendSession.getId(), kernelId);
                connectToJupyterKernel(kernelId, frontendSession);
            }

            if (jsonMessage.has("code")) {
                String kernelId = sessionKernelMap.get(frontendSession.getId());
                if (kernelId != null) {
                    String code = jsonMessage.get("code").getAsString();
                    String executeMessage = buildExecutionMessage(code);

                    Session jupyterSession = jupyterSessions.get(frontendSession.getId());
                    if (jupyterSession != null && jupyterSession.isOpen()) {
                        jupyterSession.getBasicRemote().sendText(executeMessage);
                        System.out.println("[INFO] 已将代码发送给 Jupyter Kernel");
                    } else {
                        System.err.println("[ERROR] 无法找到有效的 Jupyter Kernel 会话");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectToJupyterKernel(String kernelId, Session frontendSession) {
        try {
            String wsUrl = "ws://119.45.181.127:8888/api/kernels/" + kernelId + "/channels";

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            ClientEndpointConfig clientConfig = ClientEndpointConfig.Builder.create()
                    .configurator(new ClientEndpointConfig.Configurator() {
                        @Override
                        public void beforeRequest(Map<String, java.util.List<String>> headers) {
                            headers.put("Sec-WebSocket-Protocol", java.util.Collections.singletonList("kernel"));
                        }
                    })
                    .build();

            container.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session jupyterSession, EndpointConfig config) {
                    System.out.println("[INFO] 已连接到 Jupyter Kernel Gateway");
                    jupyterSessions.put(frontendSession.getId(), jupyterSession);

                    jupyterSession.addMessageHandler(String.class, message -> {
                        System.out.println("[RECEIVED] 来自 Jupyter Kernel: " + message);
                        try {
                            frontendSession.getBasicRemote().sendText(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onError(Session session, Throwable thr) {
                    System.err.println("[ERROR] Jupyter Kernel 连接错误");
                    thr.printStackTrace();
                }

                @Override
                public void onClose(Session session, CloseReason closeReason) {
                    System.out.println("[INFO] Jupyter 连接关闭: " + closeReason);
                }
            }, clientConfig, new URI(wsUrl));

        } catch (Exception e) {
            System.err.println("[ERROR] 连接 Jupyter Kernel 出错");
            e.printStackTrace();
        }
    }

    private String buildExecutionMessage(String codeToRun) {
        JsonObject header = new JsonObject();
        header.addProperty("msg_id", java.util.UUID.randomUUID().toString());
        header.addProperty("username", "username");
        header.addProperty("session", java.util.UUID.randomUUID().toString());
        header.addProperty("msg_type", "execute_request");
        header.addProperty("version", "5.3");

        JsonObject content = new JsonObject();
        content.addProperty("code", codeToRun);
        content.addProperty("silent", false);
        content.addProperty("store_history", true);
        content.add("user_expressions", new JsonObject());
        content.addProperty("allow_stdin", false);
        content.addProperty("stop_on_error", true);

        JsonObject message = new JsonObject();
        message.add("header", header);
        message.add("parent_header", new JsonObject());
        message.add("metadata", new JsonObject());
        message.add("content", content);

        message.add("buffers", new com.google.gson.JsonArray());
        return message.toString();
    }


    @OnClose
    public void onClose(Session session) {
        System.out.println("[INFO] 前端 WebSocket 已关闭: " + session.getId());
        frontendSessions.remove(session.getId());

        Session jupyterSession = jupyterSessions.remove(session.getId());
        if (jupyterSession != null) {
            try {
                jupyterSession.close();
                System.out.println("[INFO] 已关闭对应的 Jupyter 会话");
            } catch (Exception e) {
                System.err.println("[ERROR] 关闭 Jupyter 会话失败");
                e.printStackTrace();
            }
        }

        sessionKernelMap.remove(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("[ERROR] 前端 WebSocket 错误: " + session.getId());
        throwable.printStackTrace();
    }
}
