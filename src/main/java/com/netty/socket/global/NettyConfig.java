package com.netty.socket.global;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: lingjun.jlj
 * @date: 2019/10/20 16:15
 * @description: netty 全去配置
 */
public class NettyConfig {

    /**
     * 存储每一个客户端接入进来时的channel对象
     */
    public final static ChannelGroup GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 本地存储一份map <channel, 1>
     */
    public final static Map<String, String> LOCAL_CHANNEL_MAP = new ConcurrentHashMap<>();

    /**
     * 本地存储一份map <tokey, channel>  送达到key，channel
     */
    public final static Map<String, List<String>> LOCAL_CHANNEL_LIST_MAP = new ConcurrentHashMap<>();
}
