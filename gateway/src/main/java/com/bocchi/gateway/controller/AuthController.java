package com.bocchi.gateway.controller;

import com.bocchi.auth.JwtService;
import com.bocchi.properties.JwtProperties;
import com.bocchi.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    /**
     * 检查访问令牌是否有效
     *
     * @param userToken
     * @param adminToken
     * @return
     */
    @GetMapping("/check-token")
    public ResponseEntity<Void> checkToken(@RequestHeader(value = "authentication", required = false) String userToken,
                                           @RequestHeader(value = "token", required = false) String adminToken) {
        log.info("Checking token");

        if ((userToken != null && jwtService.validateToken(userToken, jwtProperties.getUserSecretKey())) ||
                (adminToken != null && jwtService.validateToken(adminToken, jwtProperties.getAdminSecretKey()))) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * 根据刷新令牌对访问令牌进行刷新
     * 先判断刷新令牌是否在黑名单中
     * 尝试解析刷新令牌
     *
     * @param userRefreshToken
     * @param adminRefreshToken
     * @return
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(@RequestHeader(value = "refresh_authentication", required = false) String userRefreshToken,
                                               @RequestHeader(value = "refresh_token", required = false) String adminRefreshToken) {
        log.info("Refreshing token");
        log.info("refresh token: {}", adminRefreshToken);
        log.info("refresh authentication: {}", userRefreshToken);

        if (userRefreshToken != null && jwtService.validateToken(userRefreshToken, jwtProperties.getUserRefreshSecretKey())) {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserRefreshSecretKey(), userRefreshToken);
            Map<String, Object> claimsMap = new HashMap<>(claims);
            String newAccessToken = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claimsMap);
            log.info("access_authentication : {}", newAccessToken);
            return ResponseEntity.ok(newAccessToken);
        } else if (adminRefreshToken != null && jwtService.validateToken(adminRefreshToken, jwtProperties.getAdminRefreshSecretKey())) {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminRefreshSecretKey(), adminRefreshToken);
            Map<String, Object> claimsMap = new HashMap<>(claims);
            String newAccessToken = JwtUtil.createJWT(jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTtl(), claimsMap);
            log.info("new access_token : {}", newAccessToken);
            return ResponseEntity.ok(newAccessToken);
        } else {
            log.info("Refreshing token failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
