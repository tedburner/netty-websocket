package com.netty.socket.websocket;

import com.netty.socket.constant.WsConstants;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author: lingjun.jlj
 * @date: 2019/10/19 21:16
 * @description:
 */
public class NioWebsocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                //设置日志监听器，并且日志几倍为debug，方便观察
                // .addLast("logging", new LoggingHandler("DEBUG"))
                //设置解码器
                .addLast(WsConstants.HTTP_CODEC, new HttpServerCodec())
                //聚合器，使用websocket时用到
                .addLast(WsConstants.AGGREGATOR, new HttpObjectAggregator(WsConstants.MAX_CONTENT_LENGTH))
                //用于大数据的分区传输
                .addLast(WsConstants.HTTP_CHUNKED, new ChunkedWriteHandler())
                //.addLast(new WebSocketServerProtocolHandler("/ws"))
                //自定义业务handler处理
                .addLast(WsConstants.HANDLER, new NioWebsocketHandler());
    }
}
