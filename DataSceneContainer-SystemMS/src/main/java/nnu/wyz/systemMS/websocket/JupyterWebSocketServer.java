package nnu.wyz.systemMS.websocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/webSocket/execute/{executeType}")
@Component
public class JupyterWebSocketServer {

    @Value("${codeOutPutHub}")
    private String codeOutPutHub;

    @Value("${JupyterInnerOutPutHub}")
    private String JupyterInnerOutPutHub;

    private static final ConcurrentHashMap<String, Session> frontendSessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Session> jupyterSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionKernelMap = new ConcurrentHashMap<>();
    private final Map<String, String> sessionTypeMap = new ConcurrentHashMap<>();
    private final Map<String, File> sessionTempFiles = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("executeType") String executeType) {
        System.out.println("[INFO] WebSocket连接建立: " + session.getId() + " 类型: " + executeType);
        frontendSessions.put(session.getId(), session);
        sessionTypeMap.put(session.getId(), executeType); // 保存类型
    }

    @OnMessage
    public void onMessage(String message, Session frontendSession) {
        System.out.println("[MESSAGE]: " + message +'\n' + "[FRONTEDSESSION]: " + frontendSession);
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
                    String executeMessage;

                    if (code.length() > 10 * 1024) {
                        // 长代码处理：写入临时 py 文件
                        try {
                            String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
                            File dir = new File(codeOutPutHub + "/tempScript/" + kernelId);
                            if (!dir.exists()) dir.mkdirs();

                            File tempScript = new File(dir, "temp_" + uuid + ".py");
                            try (FileWriter writer = new FileWriter(tempScript)) {
                                writer.write(code);
                            }

                            sessionTempFiles.put(frontendSession.getId(), tempScript);

                            // 生成执行该文件的指令
                            String execCode = "!python " + JupyterInnerOutPutHub + "/tempScript/" + kernelId + "/temp_" + uuid + ".py";
                            executeMessage = buildExecutionMessage(execCode);

                            System.out.println("[INFO] 长代码写入并准备执行");
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                    } else {
                        executeMessage = buildExecutionMessage(code);
                    }

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

            // ⚠️ 必须设置较大的 buffer，否则即使使用 Partial 也会失败
            container.setDefaultMaxTextMessageBufferSize(10 * 1024 * 1024); // 10MB
            container.setDefaultMaxBinaryMessageBufferSize(10 * 1024 * 1024);

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

                    jupyterSession.addMessageHandler(new MessageHandler.Whole<String>() {
                        @Override
                        public void onMessage(String message) {
                            System.out.println("[RECEIVED] 来自 Jupyter Kernel (完整消息): " + message);
                            try {
                                String type = sessionTypeMap.get(frontendSession.getId());
                                if ("Inner".equalsIgnoreCase(type)) {
                                    JsonObject msgObj = JsonParser.parseString(message).getAsJsonObject();
                                    msgObj.addProperty("executeType", "inner");
                                    frontendSession.getBasicRemote().sendText(msgObj.toString());
                                } else {
                                    frontendSession.getBasicRemote().sendText(message);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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

        // 删除临时脚本文件
        File temp = sessionTempFiles.remove(session.getId());
        if (temp != null && temp.exists()) {
            if (temp.delete()) {
                System.out.println("[INFO] 已删除临时脚本: " + temp.getAbsolutePath());
            } else {
                System.err.println("[WARN] 临时脚本删除失败: " + temp.getAbsolutePath());
            }
        }

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
