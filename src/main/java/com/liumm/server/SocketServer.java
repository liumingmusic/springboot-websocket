package com.liumm.server;

import com.liumm.entity.ClientEntity;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;


/**
 * 描述: websocket 服务端类
 *
 * 所有的websocket回话链接有此类进行创建，后续的群发，回话、聊天使用控制类调用此类进行发送。
 * 重点为区分对应的回话就行
 *
 * @Author liumm
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 * @Date 2019-08-18 15:15
 */
@Slf4j
@Component(value = "socketServer")
@ServerEndpoint(value = "/socketServer/{userName}")
public class SocketServer {

    /**
     * 用线程安全的CopyOnWriteArraySet来存放客户端连接的信息
     */
    private static CopyOnWriteArraySet<ClientEntity> socketServers = new CopyOnWriteArraySet<>();

    /**
     * websocket封装的session
     * 信息推送上下文，就通过session进行发送
     */
    private Session session;

    /**
     * 服务端的userName,因为用的是set，每个客户端的username必须不一样，否则会被覆盖。
     * 要想完成ui界面聊天的功能，服务端也需要作为客户端来接收后台推送用户发送的信息
     */
    private final static String SYS_USERNAME = "liumm";


    /**
     * 用户连接时触发，我们将其添加到
     * 保存客户端连接信息的socketServers中
     *
     * @param session  上下文回话
     * @param userName 用户名
     */
    @OnOpen
    public void open(Session session, @PathParam(value = "userName") String userName) {
        //每次出发新的连接都会进行添加
        this.session = session;
        socketServers.add(new ClientEntity(userName, session));
        log.info("客户端:【{}】连接成功", userName);
    }

    /**
     * 收到客户端发送信息时触发
     *
     * @param message 消息
     */
    @OnMessage
    public void onMessage(String message) {
        ClientEntity clientEntity = socketServers
                .stream()
                .filter(cli -> cli.getSession() == session)
                .collect(Collectors.toList())
                .get(0);
        sendMessage(clientEntity.getUserName() + ": " + message, SYS_USERNAME);
        log.info("客户端:【{}】发送信息:{}", clientEntity.getUserName(), message);
    }

    /**
     * 连接关闭触发，通过sessionId来移除
     * socketServers中客户端连接信息
     */
    @OnClose
    public void onClose() {
        socketServers.forEach(clientEntity -> {
            if (clientEntity.getSession().getId().equals(session.getId())) {
                log.info("客户端:【{}】断开连接", clientEntity.getUserName());
                socketServers.remove(clientEntity);
            }
        });
    }

    /**
     * 发生错误时触发
     *
     * @param error 错误时出发
     */
    @OnError
    public void onError(Throwable error) {
        socketServers.forEach(clientEntity -> {
            if (clientEntity.getSession().getId().equals(session.getId())) {
                socketServers.remove(clientEntity);
                log.error("客户端:【{}】发生异常", clientEntity.getUserName());
                error.printStackTrace();
            }
        });
    }

    /**
     * 信息发送的方法，通过客户端的userName
     * 拿到其对应的session，调用信息推送的方法
     *
     * @param message  消息
     * @param userName 用户名
     */
    private synchronized static void sendMessage(String message, String userName) {
        //群发，全部发送数据
        socketServers.forEach(clientEntity -> {
            if (userName.equals(clientEntity.getUserName())) {
                try {
                    clientEntity.getSession().getBasicRemote().sendText(message);
                    log.info("服务端推送给客户端 :【{}】", clientEntity.getUserName(), message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * @return 当前回话总数
     */
    public synchronized int getOnlineNum() {
        return socketServers
                .stream()
                .filter(clientEntity -> !clientEntity.getUserName().equals(SYS_USERNAME))
                .collect(Collectors.toList())
                .size();
    }

    /**
     * 获取在线用户名，前端界面需要用到
     *
     * @return 获取在线用户名
     */
    public synchronized List<String> getOnlineUsers() {
        return socketServers.stream()
                .filter(clientEntity -> !clientEntity.getUserName().equals(SYS_USERNAME))
                .map(ClientEntity::getUserName)
                .collect(Collectors.toList());
    }

    /**
     * @param message 群发消息
     */
    public synchronized static void sendAll(String message) {
        //群发，不能发送给服务端自己
        socketServers
                .stream()
                .filter(cli -> !cli.getUserName().equals(SYS_USERNAME))
                .forEach(clientEntity -> {
                    try {
                        clientEntity.getSession().getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        log.info("服务端推送给所有客户端 :【{}】", message);
    }

    /**
     * 多个人发送给指定的几个用户
     *
     * @param message 消息
     * @param persons 指定发送的用户集合
     */
    public synchronized void SendMany(String message, String[] persons) {
        for (String userName : persons) {
            sendMessage(message, userName);
        }
    }
}
