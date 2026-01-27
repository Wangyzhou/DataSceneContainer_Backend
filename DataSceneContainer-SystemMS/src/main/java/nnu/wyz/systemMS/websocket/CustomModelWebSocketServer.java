package nnu.wyz.systemMS.websocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nnu.wyz.systemMS.config.PythonConfig;
import nnu.wyz.systemMS.dao.DscCode.GeoActModelDAO;
import nnu.wyz.systemMS.dao.DscCode.GeoActScriptFileDAD;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActModel;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActScriptFile;
import nnu.wyz.systemMS.service.iml.GeoActServiceImpl;
import nnu.wyz.systemMS.utils.SpringContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;


@ServerEndpoint("/webSocket/execute/model")
@Component
public class CustomModelWebSocketServer {

    private static final ConcurrentHashMap<String, Session> frontendSessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Session> localPythonSessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<String, ScheduledFuture<?>> heartbeatTasks = new ConcurrentHashMap<>();
    // 存储sessionId和执行文件名的映射
    private final Map<String, String> sessionFileMap = new ConcurrentHashMap<>();

    // Spring Bean（通过 SpringContextHolder 获取）
    private GeoActServiceImpl geoActServiceImpl;
    private GeoActScriptFileDAD geoActScriptFileDAD;
    private GeoActModelDAO geoActModelDAO;
    private String JupyterInnerOutPutHub;
    private String pythonDockerIp;

    // 必须显式提供无参构造函数
    public CustomModelWebSocketServer() {
        // 这里不能直接用 @Autowired，因为 @ServerEndpoint 不归 Spring 管
        this.geoActServiceImpl = SpringContextHolder.getBean(GeoActServiceImpl.class);
        this.geoActScriptFileDAD = SpringContextHolder.getBean(GeoActScriptFileDAD.class);
        this.geoActModelDAO = SpringContextHolder.getBean(GeoActModelDAO.class);
        this.JupyterInnerOutPutHub = SpringContextHolder.getApplicationContext().getEnvironment()
                .getProperty("JupyterInnerOutPutHub");

        PythonConfig pythonConfig = SpringContextHolder.getBean(PythonConfig.class);
        this.pythonDockerIp = pythonConfig.getPythonDockerIp();
    }

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
            URI uri = new URI("ws://"+ pythonDockerIp +"/ws/model"); // FastAPI WebSocket

            ClientEndpointConfig clientConfig = ClientEndpointConfig.Builder.create().build();

            container.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session localSession, EndpointConfig config) {
                    System.out.println("[INFO] 已连接到本地 Python 执行服务");
                    localPythonSessions.put(frontendSession.getId(), localSession);

                    try {
                        // 拼接文件路径
                        String modelId = jsonMessage.get("toolId").getAsString();
                        String exFileName = jsonMessage.get("exFileName").getAsString();
                        Optional<GeoActModel> modelOpt = geoActModelDAO.findById(modelId);
                        if (modelOpt.isPresent()) {
                            String modelFilePath = JupyterInnerOutPutHub + File.separator + "GeoActModel" ;
                            System.out.println("[INFO] 模型代码文件为目录："+ modelFilePath);
                            jsonMessage.addProperty("file_ex_name", exFileName);
                            jsonMessage.addProperty("file_path", modelFilePath);
                            
                            // 存储sessionId和文件名的映射
                            sessionFileMap.put(frontendSession.getId(), modelFilePath + File.separator + exFileName);
                        }
                        System.out.println("[INFO] 模型执行参数为：" + jsonMessage);
                        localSession.getBasicRemote().sendText(jsonMessage.toString());
                        System.out.println("[INFO] 已将文件执行请求发送到 FastAPI 服务");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    localSession.addMessageHandler(new MessageHandler.Whole<String>() {
                        @Override
                        public void onMessage(String msg) {
                            try {
                                frontendSession.getBasicRemote().sendText(msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
            }, clientConfig, uri);

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
        
        // 在WebSocket关闭时删除对应的执行文件
        String filePath = sessionFileMap.remove(session.getId());
        if (filePath != null) {
            try {
                File file = new File(filePath);
                if (file.exists()) {
                    if (file.delete()) {
                        System.out.println("[INFO] 已删除执行文件: " + filePath);
                    } else {
                        System.err.println("[ERROR] 无法删除执行文件: " + filePath);
                    }
                } else {
                    System.out.println("[INFO] 执行文件不存在: " + filePath);
                }
            } catch (Exception e) {
                System.err.println("[ERROR] 删除执行文件时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("[ERROR] 前端 WebSocket 异常: " + session.getId());
        throwable.printStackTrace();
    }
}
