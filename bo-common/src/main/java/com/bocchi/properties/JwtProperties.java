package com.bocchi.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bocchi.jwt")
@Data
public class JwtProperties {

    /**
     * 管理端员工生成jwt令牌相关配置
     */
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;
    private String adminRefreshSecretKey;
    private long adminRefreshTtl;
    private String adminRefreshTokenName;

    /**
     * 用户端生成jwt令牌相关配置
     */
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;
    private String userRefreshSecretKey;
    private long userRefreshTtl;
    private String userRefreshTokenName;
}
