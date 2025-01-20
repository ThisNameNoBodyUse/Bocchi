package com.bocchi.gateway.config;

import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class SentinelConfigChecker {

    @Value("${spring.cloud.sentinel.transport.dashboard}")
    private String dashboard;

    @Value("${spring.cloud.sentinel.transport.client-ip}")
    private String clientIp;

    @Value("${spring.cloud.sentinel.transport.port}")
    private String clientPort;

    @PostConstruct
    public void initSentinel() {
        // Sentinel Dashboard配置
        System.setProperty(TransportConfig.CONSOLE_SERVER, dashboard);
        // Sentinel Client配置
        System.setProperty(TransportConfig.HEARTBEAT_CLIENT_IP, clientIp);
        System.setProperty(TransportConfig.SERVER_PORT, clientPort);

        // 打印配置信息
        System.out.println("Sentinel Dashboard: " + System.getProperty(TransportConfig.CONSOLE_SERVER));
        System.out.println("Sentinel Client IP: " + System.getProperty(TransportConfig.HEARTBEAT_CLIENT_IP));
        System.out.println("Sentinel Client Port: " + System.getProperty(TransportConfig.SERVER_PORT));
    }
}
