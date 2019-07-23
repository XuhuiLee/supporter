package com.createarttechnology.supporter.redis;

import com.createarttechnology.config.Config;
import com.createarttechnology.config.ConfigFactory;
import com.createarttechnology.config.ConfigWatcher;
import com.createarttechnology.logger.Logger;
import org.springframework.beans.factory.FactoryBean;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by lixuhui on 2019/7/22.
 */
public class JedisFactory implements FactoryBean {

    private static final Logger logger = Logger.getLogger(JedisFactory.class);

    private String configName;
    private static volatile Jedis INSTANCE;

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


                try {
                    JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), host, port, timeout, password);
                    if (INSTANCE == null) {
                        INSTANCE = jedisPool.getResource();
                    }
                    logger.info("[use redis: {}:{}]", host, port);
                } catch (Exception e) {
                    logger.error("init JedisFactory error, configName={}, e:", configName, e);
                }
            }
        });
    }


    @Override
    public Object getObject() throws Exception {
        if (INSTANCE == null) {
            synchronized (JedisFactory.class) {
                if (INSTANCE == null) {
                    init();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public Class<?> getObjectType() {
        return Jedis.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
