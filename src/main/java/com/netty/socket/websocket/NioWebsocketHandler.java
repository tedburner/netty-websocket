package com.netty.socket.websocket;

import com.netty.socket.constant.RedisConstants;
import com.netty.socket.constant.WSConstants;
import com.netty.socket.global.NettyConfig;
import com.netty.socket.util.RedisUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author: lingjun.jlj
 * @date: 2019/10/19 21:15
 * @description:
 */
@Slf4j
public class NioWebsocketHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketServerHandshaker handShaker;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            //以http请求形式接入，但是走的是websocket
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            //处理websocket客户端的消息
            handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    /**
     * 客户端与服务端创建连接诶是调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //添加连接
        log.debug("客户端加入连接：" + ctx.channel());
        NettyConfig.GROUP.add(ctx.channel());
        String key = ctx.channel().id().asLongText();
        NettyConfig.LOCAL_CHANNEL_MAP.put(key, "1");
    }

    /**
     * 客户端与服务端断开连接的时候调用
     *
     * @param ctx ctx
     * @throws Exception Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //断开连接
        log.debug("客户端断开连接：" + ctx.channel());
        NettyConfig.GROUP.remove(ctx.channel());
        String key = ctx.channel().id().asLongText();
        NettyConfig.LOCAL_CHANNEL_MAP.remove(key);
    }

    /**
     * 服务端接收客户端发送过来的数据结束之后调用
     *
     * @param ctx ctx
     * @throws Exception Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        log.info("flush 数据 {} {} ", ctx.name(), ctx.channel().id().asLongText());
    }

    /**
     * 处理客户端与服务器之间的websocket业务
     *
     * @param ctx
     * @param frame
     */
    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            handShaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        // 判断是否ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(
                    new PongWebSocketFrame(frame.content().retain()));
            log.debug("接收到ping消息");
            return;
        }
        // 本例程仅支持文本消息，不支持二进制消息，抛出异常
        if (!(frame instanceof TextWebSocketFrame)) {
            log.debug("本例程仅支持文本消息，不支持二进制消息");
            throw new UnsupportedOperationException(String.format(
                    "%s frame types not supported", frame.getClass().getName()));
        }
        // 获取客户端向服务器端发送的消息
        String requestMsg = ((TextWebSocketFrame) frame).text();

        log.debug("服务端收到：" + requestMsg);
//        TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString()
//                + ctx.channel().id() + "：" + requestMsg);

        //发布到redis,订阅列表，进行广播
        String keyChannel = ctx.channel().id().asLongText();
        ChannelId channelId = ctx.channel().id();
        RedisUtils.set(keyChannel, requestMsg);
        RedisUtils.set(keyChannel + "_id", channelId);
        RedisUtils.push(RedisConstants.WS_QUEUE_CHANNEL_ID, keyChannel);


    }

    /**
     * 处理客户端向服务器发起的 http 握手请求
     *
     * @param ctx
     * @param request
     */
    private void handleHttpRequest(ChannelHandlerContext ctx,
                                   FullHttpRequest request) {
        //要求Upgrade为websocket，过滤掉get/Post
        if (!request.decoderResult().isSuccess()
                || (!"websocket".equals(request.headers().get("Upgrade")))) {
            //若不是websocket方式，则创建BAD_REQUEST的req，返回给客户端
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                WSConstants.WEB_SOCKET_URL, null, false);
        handShaker = wsFactory.newHandshaker(request);
        if (handShaker == null) {
            WebSocketServerHandshakerFactory
                    .sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handShaker.handshake(ctx.channel(), request);
        }
    }

    /**
     * 服务器端想客户发送消息
     *
     * @param ctx
     * @param request
     * @param response
     */
    private static void sendHttpResponse(ChannelHandlerContext ctx,
                                         FullHttpRequest request,
                                         DefaultFullHttpResponse response) {
        // 返回应答给客户端
        if (response.status().code() != WSConstants.HTTP_OK) {
            ByteBuf buf = Unpooled.copiedBuffer(response.status().toString(),
                    CharsetUtil.UTF_8);
            response.content().writeBytes(buf);
            buf.release();
            log.warn("不成功响应");
        }
        //服务器端想客户端发送数据
        ChannelFuture channelFuture = ctx.channel().writeAndFlush(response);
        // 如果是非Keep-Alive，关闭连接
        if (!HttpUtil.isKeepAlive(request) || response.status().code() != WSConstants.HTTP_OK) {
            channelFuture.addListener(ChannelFutureListener.CLOSE);
            log.info("websocket 连接关闭");
        }
    }
}
