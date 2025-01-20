package com.bocchi.interceptor;

import cn.hutool.core.util.StrUtil;
import com.bocchi.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * 信息拦截器(第二次经过)
 * 不用进行登录拦截，因为登录拦截已经被网关做了
 * 这里只用进行用户信息的获取，统一进行放行
 */
@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        log.info("请求路径 : {}", requestURI);

        // 判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            // 当前拦截到的不是动态方法，直接放行
            return true;
        }

        // 从请求头中获取用户信息
        String userId = request.getHeader("user-info");
        String adminId = request.getHeader("admin-info");

        log.info("接收到的请求头 userId: {}, adminId: {}", userId, adminId);

        // 判断并存入ThreadLocal
        if (StrUtil.isNotBlank(userId)) { //不为空且长度不为0
            log.info("当前用户id：{}", userId);
            BaseContext.setCurrentUserId(Long.valueOf(userId));
        } else if (StrUtil.isNotBlank(adminId)) {
            log.info("当前管理员id：{}", adminId);
            BaseContext.setCurrentAdminId(Long.valueOf(adminId));
        } else {
            // 如果没有用户信息或管理员信息，返回401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理用户和管理员信息
        BaseContext.removeCurrentUserId();
        BaseContext.removeCurrentAdminId();
    }
}
