package com.bocchi.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class JwtUtil {
    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
     * @param secretKey jwt秘钥
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     * @return
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 签名算法
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        // JWT过期时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                // 设置签名算法和秘钥
                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8))
                // 设置jti (jwt的id)
                .setId(UUID.randomUUID().toString())
                // 设置过期时间
                .setExpiration(exp);

        return builder.compact();
    }

    /**
     * Token解密
     *
     * @param secretKey jwt秘钥
     * @param token     加密后的token
     * @return
     */
    public static Claims parseJWT(String secretKey, String token) {
        Claims claims = Jwts.parser()
                // 设置签名秘钥
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                // 设置需要解析的jwt
                .parseClaimsJws(token).getBody();
        return claims;
    }

    /**
     * 获取JWT令牌的过期时间
     *
     * @param secretKey jwt秘钥
     * @param jwt       JWT令牌
     * @return 过期时间（毫秒）
     */
    public static long getExpireTime(String secretKey, String jwt) {
        Claims claims = parseJWT(secretKey, jwt);
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    /**
     * 获取JWT令牌的jti
     *
     * @param secretKey jwt秘钥
     * @param jwt       JWT令牌
     * @return jti
     */
    public static String getJti(String secretKey, String jwt) {
        Claims claims = parseJWT(secretKey, jwt);
        return claims.getId();
    }

}
