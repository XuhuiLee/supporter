package com.createarttechnology.supporter.redis;

import com.createarttechnology.config.Config;
import com.createarttechnology.config.ConfigFactory;
import com.createarttechnology.config.ConfigWatcher;
import com.createarttechnology.logger.Logger;
import com.google.common.reflect.Reflection;
import org.springframework.beans.factory.FactoryBean;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.commands.JedisCommands;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by lixuhui on 2019/7/22.
 */
public class JedisFactory implements FactoryBean {

    private static final Logger logger = Logger.getLogger(JedisFactory.class);

    private String configName;
    private static volatile JedisPool POOL;

    public void setConfig(String configName) {
        this.configName = configName;
    }

    private void init() {
        ConfigFactory.load(configName, new ConfigWatcher() {
            @Override
            public void changed(Config config) {
                String host = config.getString("host", "127.0.0.1");
                int port = config.getInt("port", 6379);
                int timeout = config.getInt("timeout", 3000);
                String password = config.getString("password", null);
                int database = config.getInt("database", 1);
                int maxTotal = config.getInt("maxTotal", 5);

                try {
                    JedisPool oldPool = POOL;
                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    poolConfig.setMaxTotal(maxTotal);
                    POOL = new JedisPool(poolConfig, host, port, timeout, password, database);
                    if (oldPool != null) {
                        oldPool.destroy();
                    }
                    logger.info("[use redis: {}:{}, database:{}]", host, port, database);
                } catch (Exception e) {
                    logger.error("init JedisFactory error, configName={}, e:", configName, e);
                }
            }
        });
    }

    @Override
    public Object getObject() throws Exception {
        if (POOL == null) {
            synchronized (JedisFactory.class) {
                if (POOL == null) {
                    init();
                }
            }
        }
        return Reflection.newProxy(JedisCommands.class, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                try {
                    Jedis jedis = POOL.getResource();
                    Object result = method.invoke(jedis, args);
                    jedis.close();
                    return result;
                } catch (JedisConnectionException | ClassCastException e) {
                    logger.info("error, e:", e);
                    return null;
                }
            }
        });
    }

    @Override
    public Class<?> getObjectType() {
        return JedisCommands.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
