package com.microport.healthcert.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成、解析和验证JWT Token
 * 
 * @author system
 * @date 2024
 */
@Component
public class JwtUtil {

    /**
     * JWT密钥（从application.yml读取）
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token过期时间（毫秒，从application.yml读取）
     */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 获取签名密钥
     * 
     * @return 签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成Token
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param userType 用户类型（employee/admin）
     * @return JWT Token字符串
     */
    public String generateToken(Long userId, String username, String userType) {
        // 创建Claims，存储用户信息
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("userType", userType);

        // 计算过期时间
        Date expirationDate = new Date(System.currentTimeMillis() + expiration);

        // 生成Token
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从Token中获取Claims
     * 
     * @param token JWT Token
     * @return Claims对象
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从Token获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 从Token获取用户名
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return (String) claims.get("username");
    }

    /**
     * 从Token获取用户类型
     * 
     * @param token JWT Token
     * @return 用户类型（employee/admin）
     */
    public String getUserTypeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return (String) claims.get("userType");
    }

    /**
     * 验证Token有效性
     * 
     * @param token JWT Token
     * @return true表示Token有效，false表示Token无效
     */
    public boolean validateToken(String token) {
        try {
            // 尝试解析Token（如果解析成功，说明Token格式正确）
            getClaimsFromToken(token);
            // 检查是否过期
            return !isTokenExpired(token);
        } catch (Exception e) {
            // 解析失败或过期，返回false
            return false;
        }
    }

    /**
     * 判断Token是否过期
     * 
     * @param token JWT Token
     * @return true表示已过期，false表示未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            // 判断过期时间是否在当前时间之前
            return expiration.before(new Date());
        } catch (Exception e) {
            // 解析失败，视为已过期
            return true;
        }
    }
}

