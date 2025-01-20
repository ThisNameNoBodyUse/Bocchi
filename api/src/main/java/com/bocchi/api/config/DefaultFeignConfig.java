package com.bocchi.api.config;


import com.bocchi.context.BaseContext;
import com.bocchi.json.JacksonObjectMapper;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.optionals.OptionalDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Feign配置(第三次经过)
 * 比如当前微服务从网关获取到了用户信息保存到ThreadLocal中，并且需要调用其他微服务时
 * Feign就要把这个用户信息从ThreadLocal中取出来，封装到header中发送请求给其他微服务
 */
@Slf4j
public class DefaultFeignConfig {

    @Bean
    public HttpMessageConverters customConverters() {
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        jacksonConverter.setObjectMapper(new JacksonObjectMapper());
        return new HttpMessageConverters(jacksonConverter);
    }

    @Bean
    public Decoder feignDecoder() {
        return new OptionalDecoder(new ResponseEntityDecoder(new SpringDecoder(() -> customConverters())));
    }

    @Bean
    public Encoder feignEncoder() {
        return new SpringEncoder(() -> customConverters());
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                Long currentUserId = BaseContext.getCurrentUserId();
                Long currentAdminId = BaseContext.getCurrentAdminId();

                if (currentUserId != null) {
                    template.header("user-info", currentUserId.toString());
                    log.info("添加请求头 user-info: {}", currentUserId);
                }

                if (currentAdminId != null) {
                    template.header("admin-info", currentAdminId.toString());
                    log.info("添加请求头 admin-info: {}", currentAdminId);
                }
            }
        };
    }
}
