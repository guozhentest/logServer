package com.leantech.agent.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // 随机生成 HS256 密钥（生产环境建议固定配置）
    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // 24小时
    long expiration = 86400000L;

    /**
     * 生成 Token
     */
    public String generateToken(String username, String role) {

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * 解析 Token 并返回 Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)        // 替换 parserBuilder.setSigningKey()
                .build()
                .parseSignedClaims(token)
                .getPayload();          // 替换 parseClaimsJws(token).getBody()
    }
}