package com.createarttechnology.supporter.mysql;

import com.createarttechnology.config.Config;
import com.createarttechnology.config.ConfigFactory;
import com.createarttechnology.config.ConfigWatcher;
import com.createarttechnology.logger.Logger;
import com.google.common.base.Preconditions;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.FactoryBean;

/**
 * Created by lixuhui on 2018/11/14.
 */
public class DataSourceFactory implements FactoryBean {

    private static final Logger logger = Logger.getLogger(DataSourceFactory.class);

    private String configName;
    private static volatile ComboPooledDataSource INSTANCE;

    public void setConfig(String configName) {
        this.configName = configName;
    }

    private void init() {
        final ComboPooledDataSource newInstance = new ComboPooledDataSource();
        ConfigFactory.load(configName, new ConfigWatcher() {
            @Override
            public void changed(Config config) {
                String username = config.getString("username", null);
                String password = config.getString("password", null);
                String jdbcUrl = config.getString("jdbcUrl", null);
                Preconditions.checkNotNull(username);
                Preconditions.checkNotNull(password);
                Preconditions.checkNotNull(jdbcUrl);

                try {
                    newInstance.setUser(username);
                    newInstance.setPassword(password);
                    newInstance.setJdbcUrl(jdbcUrl);
                    newInstance.setDriverClass(config.getString("driverClass", "com.mysql.jdbc.Driver"));
                    newInstance.setInitialPoolSize(config.getInt("initialPoolSize", 3));
                    newInstance.setMaxPoolSize(config.getInt("maxPoolSize", 10));
                    newInstance.setMinPoolSize(config.getInt("minPoolSize", 3));
                    newInstance.setIdleConnectionTestPeriod(config.getInt("idleConnectionTestPeriod", 120));
                    newInstance.setAutoCommitOnClose(config.getBoolean("autoCommitOnClose", false));
                    newInstance.setCheckoutTimeout(config.getInt("checkoutTimeout", 3000));
                    newInstance.setMaxIdleTime(config.getInt("maxIdleTime", 600));
                    newInstance.setMaxStatementsPerConnection(config.getInt("maxStatementsPerConnection", 5));
                    logger.info("[use jdbc: {}]", jdbcUrl);
                } catch (Exception e) {
                    logger.error("init DataSourceFactory error, configName={}, e:", configName, e);
                }
            }
        });
        INSTANCE = newInstance;
    }

    @Override
    public Object getObject() throws Exception {
        if (INSTANCE == null) {
            synchronized (DataSourceFactory.class) {
                if (INSTANCE == null) {
                    init();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public Class<?> getObjectType() {
        return ComboPooledDataSource.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
