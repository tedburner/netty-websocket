package com.netty.socket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: lingjun.jlj
 * @description: websocket base on netty
 */
@SpringBootApplication
public class NettySocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(NettySocketApplication.class, args);
    }

}
