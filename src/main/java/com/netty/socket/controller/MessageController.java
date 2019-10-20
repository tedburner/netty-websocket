package com.netty.socket.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: lingjun.jlj
 * @date: 2019/10/20 15:44
 * @description:
 */
@RestController
@RequestMapping(value = "/msg")
public class MessageController {

    @GetMapping(value = "/send/client/{id}")
    public void sendMsgForClient(@PathVariable("id") String id) {

        System.out.println("想客户端发送消息");
    }
}
