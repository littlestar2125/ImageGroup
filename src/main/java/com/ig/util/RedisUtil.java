package com.ig.util;

import com.ig.common.Main;
import com.ig.config.Config;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

public class RedisUtil {

    //服务器IP地址
    private static final String HOST = Config.getProperties("redis.host");
    //端口
    private static final int PORT = Integer.parseInt(Config.getProperties("redis.port"));
    //密码
    private static final String AUTH = Config.getProperties("redis.auth");
    //连接实例的最大连接数
    private static final int MAX_ACTIVE = Integer.parseInt(Config.getProperties("redis.max_active"));
    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    private static final int MAX_IDLE = Integer.parseInt(Config.getProperties("redis.max_idle"));
    //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException
    private static final int MAX_WAIT = Integer.parseInt(Config.getProperties("redis.max_wait"));
    //连接超时的时间　　
    private static final int TIMEOUT = Integer.parseInt(Config.getProperties("redis.timeout"));
    // 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    private static final boolean TEST_ON_BORROW = Boolean.parseBoolean(Config.getProperties("redis.max_active"));
    //数据库模式是16个数据库 0~15
    private static final int DEFAULT_DATABASE = Integer.parseInt(Config.getProperties("redis.default_database"));

    private static JedisPool jedisPool = null;

    static {

        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(MAX_ACTIVE);
            config.setMaxIdle(MAX_IDLE);
            config.setMaxWait(Duration.ofMillis(MAX_WAIT));
            config.setTestOnBorrow(TEST_ON_BORROW);
            config.setTestWhileIdle(true);
            jedisPool = new JedisPool(config, HOST, PORT, TIMEOUT, AUTH, DEFAULT_DATABASE);
        } catch (Exception e) {
            System.exit(0);
        }

    }

    private RedisUtil() {

    }

    /**
     * 获取Jedis实例
     */

    public synchronized static Jedis getJedis() {
        try {
            if (jedisPool != null) {
                return jedisPool.getResource();
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }

    /***
     *
     * 释放资源
     */
    public static void returnResource(final Jedis jedis) {
        if (jedis != null) {
            jedisPool.returnResource(jedis);
        }
    }
}