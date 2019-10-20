package com.netty.socket.util;

import com.netty.socket.constant.RedisConstants;
import com.netty.socket.listener.RedisMsgPubSubListener;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

/**
 * @author: lingjun.jlj
 * @date: 2019/10/20 15:17
 * @description:
 */
@Slf4j
public class RedisUtils {

    public static Jedis jedis;
    public static Jedis jedisSub;

    public static void init() {
        try {
            new Thread(RedisUtils::run).start();
            log.info("redis init...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void run() {
        jedis = new Jedis("127.0.0.1");
        jedis.auth("123456");
        jedis.connect();

        jedisSub = new Jedis("127.0.0.1");
        jedisSub.auth("123456");
        RedisMsgPubSubListener listener = new RedisMsgPubSubListener();
        jedisSub.subscribe(listener, RedisConstants.WS_QUEUE_CHANNEL_ID);
    }


    /**
     * 插入缓存
     *
     * @param key
     * @param value
     */
    public static void set(String key, String value) {
        jedis.set(key, value);
    }

    /**
     * 插入缓存
     *
     * @param key
     * @param value
     */
    public static <T> void set(String key, T value) {
        byte[] keyBytes = SerializationUtils.serialize(key);
        byte[] data = SerializationUtils.serialize(value);
        jedis.set(keyBytes, data);
    }

    /**
     * 获取缓存
     *
     * @param key
     * @return String
     */
    public static String get(String key) {
        return jedis.get(key);
    }

    /**
     * 获取缓存
     *
     * @param key
     * @return T
     */
    public static <T> T get(String key, Class<T> clazz) {
        byte[] data = jedis.get(SerializationUtils.serialize(key));
        T result = SerializationUtils.deserialize(data, clazz);
        return result;
    }

    /**
     * 删除缓存
     *
     * @param key
     * @return T
     */
    public static <T> void del(String key) {
        jedis.del(SerializationUtils.serialize(key));
    }

    /**
     * 删除缓存
     *
     * @param key
     * @return T
     */
    public static void delete(String key) {
        jedis.del(key);
    }

    /**
     * List push
     *
     * @param key
     * @param value
     */
    public static void push(String key, String value) {
        jedis.publish(key, value);
    }
}
