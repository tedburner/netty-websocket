package com.netty.socket.listener;

import com.netty.socket.global.NettyConfig;
import com.netty.socket.util.RedisUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.DefaultChannelId;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

import java.util.Date;

/**
 * @author: lingjun.jlj
 * @date: 2019/10/20 15:56
 * @description: redis 订阅监听
 */
@Slf4j
public class RedisMsgPubSubListener extends JedisPubSub {

    @Override
    public void onMessage(String channelMsgKey, String channelId) {
        if (StringUtil.isNullOrEmpty(channelId)) {
            return;
        }

        String isExists = NettyConfig.LOCAL_CHANNEL_MAP.get(channelId);
        if (StringUtil.isNullOrEmpty(isExists)) {
            //离线则丢弃，不推送
            return;
        }

        String msg = RedisUtils.get(channelId);
        if (StringUtil.isNullOrEmpty(msg)) {
            return;
        }

        ChannelId id = RedisUtils.get(channelId + "_id", DefaultChannelId.class);
        if (id == null) {
            return;
        }
        log.info("channelMsgId: {}, msg:{}", channelId, msg);
        Channel channel = NettyConfig.GROUP.find(id);
        if (channel != null) {
            String responseMsg = new Date().toString() + channel.id() + "======>" + msg;
            TextWebSocketFrame frame = new TextWebSocketFrame(responseMsg);
            channel.writeAndFlush(frame);

            RedisUtils.delete(channelId);
        }
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        log.info("channel:" + channel + "is been subscribed:" + subscribedChannels);
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        log.info("channel:" + channel + "is been unsubscribed:" + subscribedChannels);
    }
}
