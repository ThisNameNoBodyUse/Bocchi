package com.bocchi.config;

import com.bocchi.properties.AliOssProperties;
import com.bocchi.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类 ： 用于创建AliOssUtil对象
 */

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET) //只在非网关层加载
@Slf4j
public class OssConfiguration {

    @Bean
    @ConditionalOnMissingBean //保证整个Spring容器中只有一个utils对象
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties) {
        log.info("开始创建阿里云文件上传工具类对象 ： {}", aliOssProperties);
        return new AliOssUtil(aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName(),
                aliOssProperties.getCdnDomain());
    }
}
