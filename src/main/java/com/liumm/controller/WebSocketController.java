package com.liumm.controller;

import com.liumm.server.SocketServer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;


/**
 * 描述: webSocket客户端
 *
 * @Author liumm
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 * @Date 2019-08-18 15:15
 */
@Controller
public class WebSocketController {

    @Resource(name = "socketServer")
    private SocketServer socketServer;

    /**
     * 客户端页面
     *
     * @return 客户端页面
     */
    @RequestMapping(value = "/index")
    public String index() {
        return "index";
    }

    /**
     * 服务端页面
     *
     * @param model 服务端页面
     * @return 服务端页面
     */
    @RequestMapping(value = "/admin")
    public String admin(Model model) {
        model.addAttribute("num", socketServer.getOnlineNum());
        model.addAttribute("users", socketServer.getOnlineUsers());
        return "admin";
    }

    /**
     * 信息推送指定用户
     *
     * @param msg      消息
     * @param username 用户名
     * @return 消息推送
     */
    @RequestMapping("sendMsg")
    @ResponseBody
    public String sendMsg(String msg, String username) {
        String[] persons = username.split(",");
        socketServer.SendMany(msg, persons);
        return "success";
    }

    /**
     * 推送给所有在线用户
     *
     * @return 群发消息
     */
    @RequestMapping("sendAll")
    @ResponseBody
    public String sendAll(String msg) {
        SocketServer.sendAll(msg);
        return "success";
    }
}
