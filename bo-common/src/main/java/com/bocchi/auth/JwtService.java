package com.bocchi.auth;

import com.bocchi.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class JwtService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 将 JWT 的 jti 存储到 Redis 中，并设置过期时间。
     * @param jti JWT 的唯一标识符（jti）
     * @param expireTime JWT 的过期时间（毫秒）
     */
    public void storeToken(String jti, long expireTime) {
        redisTemplate.opsForValue().set(jti, "invalid", expireTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 从 Redis 中删除指定的 jti。
     * @param jti JWT 的唯一标识符（jti）
     */
    public void removeToken(String jti) {
        redisTemplate.delete(jti);
    }

    /**
     * 检查指定的 jti 是否在 Redis 中
     * @param jti JWT 的唯一标识符（jti）
     * @return 如果 jti 不在 Redis 中，则返回 true；否则返回 false
     */
    private boolean isTokenValid(String jti) {
        return !Boolean.TRUE.equals(redisTemplate.hasKey(jti));
    }

    /**
     * 验证 token 是否有效
     * @param token JWT token
     * @param secretKey JWT 秘钥
     * @return 如果 token 有效，则返回 true；否则返回 false
     */
    public boolean validateToken(String token, String secretKey) {
        try {
            Claims claims = JwtUtil.parseJWT(secretKey, token);
            String jti = claims.getId();
            return isTokenValid(jti);
        } catch (Exception e) {
            return false;
        }
    }
}

