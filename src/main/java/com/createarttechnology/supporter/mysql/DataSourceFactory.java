package com.createarttechnology.supporter.mysql;

import com.createarttechnology.config.Config;
import com.createarttechnology.config.ConfigFactory;
import com.createarttechnology.config.ConfigWatcher;
import com.google.common.base.Preconditions;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * Created by lixuhui on 2018/11/14.
 */
public class DataSourceFactory implements org.springframework.beans.factory.FactoryBean {

    private String configName;
    private ComboPooledDataSource dataSource;

    public void setConfig(String configName) {
        this.configName = configName;
    }

    public void init() {
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
                } catch (Exception e) {

                }
            }
        });
        this.dataSource = newInstance;
    }

    @Override
    public Object getObject() throws Exception {
        if (dataSource == null) {
            synchronized (this) {
                if (dataSource == null) {
                    init();
                }
            }
        }
        return dataSource;
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
