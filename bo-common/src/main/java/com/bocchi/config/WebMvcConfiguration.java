package com.bocchi.config;

import com.bocchi.interceptor.JwtInterceptor;
import com.bocchi.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET) //只在非网关层加载
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/admin/**", "/user/**") // 统一添加拦截路径
                .excludePathPatterns(
                        "/admin/employee/login",
                        "/user/user/login",
                        "/**/sendCode/**",
                        "/**/register",
                        "/user/category/**",
                        "/user/dish/**",
                        "/user/setMeal/**"
                ); // 排除路径
        log.info("拦截器配置完成!");
    }


    /**
     * 静态资源映射
     *
     * @param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始设置静态资源映射......");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 扩展Spring MVC框架的消息转换器
     * Springboot 3.0以下使用
     *
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器......");
        //创建一个消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        //需要为消息转换器设置一个对象转换器，对象转换器可以将Java对象序列转换为json数据
        converter.setObjectMapper(new JacksonObjectMapper());
        //将自己的消息转换器加入到容器中
        converters.add(0, converter);
    }

    /**
     * 配置全局CORS设置
     *
     * @return CorsFilter
     */
    @Bean
    public CorsFilter corsFilter() {
        log.info("配置跨域请求......");
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*"); // 允许所有域
        config.addAllowedHeader("*"); // 允许所有请求头
        config.addAllowedMethod("*"); // 允许所有请求方法（GET, POST, etc）
        config.setAllowCredentials(true); // 允许发送Cookie信息

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 对所有路径生效

        return new CorsFilter(source);
    }
}
