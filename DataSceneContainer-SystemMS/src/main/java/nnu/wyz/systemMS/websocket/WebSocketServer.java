package nnu.wyz.systemMS.websocket;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.systemMS.dao.DscMessageDAO;
import nnu.wyz.systemMS.dao.DscUserDAO;
import nnu.wyz.systemMS.model.entity.DscUser;
import nnu.wyz.systemMS.model.entity.Message;
import nnu.wyz.systemMS.utils.RedisCache;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@ServerEndpoint("/webSocket/{username}")
@Component
@Slf4j
public class WebSocketServer {
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static AtomicInteger onlineNum = new AtomicInteger();

    //concurrent包的线程安全Set，用来存放每个客户端对应的WebSocketServer对象。
    private static ConcurrentHashMap<String, Session> sessionPools = new ConcurrentHashMap<>();


    //发送消息
    public void sendMessage(Session session, String message) throws IOException {
        if (session != null) {
            synchronized (session) {
//                log.info("发送数据:" + message);
                session.getBasicRemote().sendText(message);
                Message msg = JSON.parseObject(message, Message.class);
                if (!msg.getType().equals("connected") && !msg.getType().equals("disconnected") && !msg.getType().equals("tool-execute")) { //除了连接消息，都入库
                    DscMessageDAO dscMessageDAO = SpringUtil.getBean(DscMessageDAO.class);
                    dscMessageDAO.insert(msg);
                }
            }
        }
    }

    //给指定用户发送信息
    public void sendInfo(String userName, String message) {
        Session session = sessionPools.get(userName);
        Message msg = JSON.parseObject(message, Message.class);
        msg.setDate(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        if (session != null) {  //用户在线
            try {
                sendMessage(session, JSON.toJSONString(msg));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            RedisCache redisCache = SpringUtil.getBean(RedisCache.class);
            List<Object> msgList = redisCache.getCacheList(userName + "-noSentMessages");
            msgList.add(JSON.toJSONString(msg));
            redisCache.deleteObject(userName + "-noSentMessages");
            redisCache.setCacheList(userName + "-noSentMessages", msgList);
        }
    }

    // 群发消息
    public void broadcast(String message) {
        Message msg = JSON.parseObject(message, Message.class);
        msg.setDate(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));    //自动获取消息时间
        DscUserDAO dscUserDAO = SpringUtil.getBean(DscUserDAO.class);
        List<String> emails = dscUserDAO.findAllByEnabled(1).stream().map(DscUser::getEmail).collect(Collectors.toList());
        for (String email : emails) {
            msg.setId(IdUtil.randomUUID());
            msg.setTo(email);    //群发给每一个用户
            Session session = sessionPools.get(email);
            if (session != null) {
                try {
                    sendMessage(session, JSON.toJSONString(msg));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                RedisCache redisCache = SpringUtil.getBean(RedisCache.class);
                List<Object> msgList = redisCache.getCacheList(email + "-noSentMessages");
                msgList.add(JSON.toJSONString(msg));
                redisCache.deleteObject(email + "-noSentMessages");
                redisCache.setCacheList(email + "-noSentMessages", msgList);
            }
        }
    }

    //建立连接成功调用
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "username") String userName){
        Session isExistSession = sessionPools.get(userName);
        if(isExistSession != null) {
            Message message = new Message();
            message.setFrom("system");
            message.setTo(userName);
            message.setType("disconnected");
            message.setTopic("连接断开");
            message.setText("您的账号在另一处登录，服务器连接断开！");
            sendInfo(userName, JSON.toJSONString(message));
            onClose(userName);
        }
        sessionPools.put(userName, session);
        addOnlineCount();
        log.info(userName + "加入webSocket！当前人数为" + onlineNum);
        RedisCache redisCache = SpringUtil.getBean(RedisCache.class);
        List<Object> noRecieveMsgs = redisCache.getCacheList(userName + "-noSentMessages");
        if (noRecieveMsgs.size() != 0) {
            log.info("noRecieveMsgs.size = " + noRecieveMsgs.size());
            noRecieveMsgs.forEach(msg -> {
                Message sendMsg = JSON.parseObject((String) msg, Message.class);
                sendInfo(sendMsg.getTo(), JSON.toJSONString(sendMsg, true));
            });
            redisCache.deleteObject(userName + "-noSentMessages");
        }
    }


    //关闭连接时调用
    @OnClose
    public void onClose(@PathParam(value = "username") String userName) {
        sessionPools.remove(userName);
        subOnlineCount();
        log.info(userName + "断开webSocket连接！当前人数为" + onlineNum);
    }

    //收到客户端信息后，根据接收人的username把消息推下去或者群发
    // to=-1群发消息
    @OnMessage
    public void onMessage(String message) throws IOException {
        log.info("server get" + message);
        Message msg = JSON.parseObject(message, Message.class);
        if (msg.getTo().equals("-1")) {     //广播消息，代表系统消息，入库
            broadcast(JSON.toJSONString(msg, true));
        } else if (msg.getTo().equals("0")) {   //代表连接消息
            Message healthMsg = new Message();
            healthMsg
                    .setTopic("连接消息")
                    .setType("connected")
                    .setFrom("system")
                    .setTo(msg.getFrom())
                    .setResource(null)
                    .setIsRead(false)
                    .setText("服务器连接成功！");
            sendInfo(msg.getFrom(), JSON.toJSONString(healthMsg, true));
        } else {    //指定消息，需要入库
            sendInfo(msg.getTo(), JSON.toJSONString(msg, true));
        }
    }

    //错误时调用
    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

    public static void addOnlineCount() {
        onlineNum.incrementAndGet();
    }

    public static void subOnlineCount() {
        if(onlineNum.get() <= 0) {
            return;
        }
        onlineNum.decrementAndGet();
    }

    public static AtomicInteger getOnlineNumber() {
        return onlineNum;
    }

    public static ConcurrentHashMap<String, Session> getSessionPools() {
        return sessionPools;
    }
}
