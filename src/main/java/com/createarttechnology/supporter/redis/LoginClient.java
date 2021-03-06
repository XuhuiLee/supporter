package com.createarttechnology.supporter.redis;

import com.createarttechnology.config.Config;
import com.createarttechnology.config.ConfigFactory;
import com.createarttechnology.config.ConfigWatcher;
import com.createarttechnology.constant.RedisKeys;
import com.createarttechnology.jutil.StringUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 登录redis校验工具，提供静态调用
 * Created by lixuhui on 2019/7/23.
 */
public final class LoginClient {

    private static volatile JedisPool POOL;

    private static final String CONFIG_NAME = "redis-blog";

    private LoginClient() {}

    static {
        synchronized (LoginClient.class) {
            ConfigFactory.load(CONFIG_NAME, new ConfigWatcher() {
                @Override
                public void changed(Config config) {
                    String host = config.getString("host", "127.0.0.1");
                    int port = config.getInt("port", 6379);
                    int timeout = config.getInt("timeout", 3000);
                    String password = config.getString("password", null);
                    int maxTotal = config.getInt("maxTotal", 5);

                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    poolConfig.setMaxTotal(maxTotal);
                    POOL = new JedisPool(poolConfig, host, port, timeout, password);
                }
            });
        }
    }

    public static boolean checkLogin(long loginKey, String loginToken) {
        if (StringUtil.isEmpty(loginToken)) {
            return false;
        }
        String redisKey = RedisKeys.REDIS_UID_PREFIX + loginKey;
        Jedis resource = POOL.getResource();
        String redisToken = resource.get(redisKey);
        resource.close();
        return loginToken.equals(redisToken);
    }

    public static void setLogin(long loginKey, String loginToken) {
        String userKey = RedisKeys.REDIS_UID_PREFIX + loginKey;
        // 保留一个月
        Jedis resource = POOL.getResource();
        resource.setex(userKey, 2592000, loginToken);
        resource.close();
    }

}
