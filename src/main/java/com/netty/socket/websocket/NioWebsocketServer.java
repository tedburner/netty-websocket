package com.netty.socket.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: lingjun.jlj
 * @date: 2019/10/19 21:15
 * @description:
 */
@Slf4j
public class NioWebsocketServer {

    private void init() {
        log.info("启动websocket服务...");
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NioWebsocketChannelInitializer());

            Channel channel = bootstrap
                    .bind(8081)
                    .sync()
                    .channel();
            log.info("websocket服务启动成功：{}", channel);
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.info("websocket服务启动失败...。异常：{}", e.getMessage());
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
            log.info("websocket 服务关闭");
        }
    }

    public static void main(String[] args) {
        new NioWebsocketServer().init();
    }
}
