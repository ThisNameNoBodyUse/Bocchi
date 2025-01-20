package com.bocchi.gateway.filters;

import com.bocchi.constant.JwtClaimsConstant;
import com.bocchi.exception.UnauthorizedException;
import com.bocchi.gateway.config.AuthProperties;
import com.bocchi.properties.JwtProperties;
import com.bocchi.utils.JwtUtil;
import com.bocchi.auth.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 全局过滤器(最开始经过)
 * 首先前端请求发送给网关
 * 网关先判断请求路径是否需要进行拦截，如果不需要则直接放行
 * 如果需要，网关检验是否携带有效的token
 * 如果否，则状态码401，拦截
 * 如果是，则将用户信息放入header中并放行
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;
    private final JwtProperties jwtProperties;
    private final JwtService jwtService;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("进入全局过滤器");
        ServerHttpRequest request = exchange.getRequest();
        log.info("Request path: {}", request.getPath().toString());

        // 放行
        if (isExclude(request.getPath().toString())) {
            log.info("Path is excluded: {}", request.getPath().toString());
            return chain.filter(exchange);
        }

        String userToken = request.getHeaders().getFirst(jwtProperties.getUserTokenName());
        String adminToken = request.getHeaders().getFirst(jwtProperties.getAdminTokenName());

        try {
            if (userToken != null) {
                log.info("User token found");
                if (jwtService.validateToken(userToken, jwtProperties.getUserSecretKey())) {
                    Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), userToken);
                    Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
                    log.info("当前用户id：{}", userId);
                    // 传递用户信息
                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(builder -> builder.header("user-info", userId.toString()))
                            .build();
                    return chain.filter(modifiedExchange);
                } else {
                    log.info("User token is invalid");
                    return unauthorizedResponse(exchange);
                }
            } else if (adminToken != null) {
                log.info("Admin token found");
                if (jwtService.validateToken(adminToken, jwtProperties.getAdminSecretKey())) {
                    Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), adminToken);
                    Long empId = Long.valueOf(claims.get(JwtClaimsConstant.EMP_ID).toString());
                    log.info("当前员工id：{}", empId);
                    // 传递管理员信息
                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(builder -> builder.header("admin-info", empId.toString()))
                            .build();
                    return chain.filter(modifiedExchange);
                } else {
                    log.info("Admin token is invalid");
                    return unauthorizedResponse(exchange);
                }
            } else {
                log.info("No token found");
                return unauthorizedResponse(exchange);
            }
        } catch (UnauthorizedException e) {
            log.error("UnauthorizedException: ", e);
            return unauthorizedResponse(exchange); // 401 状态码
        }
    }



    private boolean isExclude(String path) {
        List<String> excludePaths = authProperties.getExcludePaths();
        for (String pathPattern : excludePaths) {
            if (antPathMatcher.match(pathPattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 登录验证失败
     *
     * @param exchange
     * @return
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
