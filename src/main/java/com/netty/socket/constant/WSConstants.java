package com.netty.socket.constant;

/**
 * @author: lingjun.jlj
 * @date: 2019/10/20 15:49
 * @description:
 */
public class WSConstants {

    public static final int HTTP_OK = 200;

    /**=======Netty Handler 配置 =====*/
    public static final String LOGGING = "logging";

    public static final String HTTP_CODEC = "http-codec";

    public static final String AGGREGATOR = "aggregator";

    public static final int MAX_CONTENT_LENGTH = 65536;

    public static final String HTTP_CHUNKED = "http-chunked";

    public static final String HANDLER = "handler";


    /**=======WebSocket 配置 =====*/
    public static final int PORT = 8989;

    public static final String WEB_SOCKET_URL = "ws://localhost:" + PORT + "/ws";
}
