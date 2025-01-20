package com.bocchi.config;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfiguration {

    @Value("${redisson.address}")
    private String address;

    @Value("${redisson.password:}") // 默认为空
    private String password;

    @Value("${redisson.database}")
    private int database;

    @Value("${redisson.connectionPoolSize}")
    private int connectionPoolSize;  // 默认10

    @Value("${redisson.subscriptionConnectionPoolSize}")
    private int subscriptionConnectionPoolSize;  // 默认5

    @Value("${redisson.timeout}")
    private int timeout;  // 默认3000ms

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
            .setAddress(address)
            .setPassword(password.isEmpty() ? null : password)
            .setDatabase(database)
            .setConnectionPoolSize(connectionPoolSize)  // 设置连接池大小
            .setSubscriptionConnectionPoolSize(subscriptionConnectionPoolSize)  // 设置订阅连接池大小
            .setTimeout(timeout);  // 设置超时时间

        return Redisson.create(config);  // 创建 Redisson 客户端实例
    }
}
