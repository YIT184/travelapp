package org.example.travel.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.travel.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一的JWT工具类（替换原JwtTokenUtil，兼容原有JwtUtil）
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;  // JWT密钥（从配置文件读取）

    @Value("${jwt.expire}")
    private long expire;    // token有效期（毫秒）

    // 生成密钥（适配新版JJWT）
    private SecretKey getSecretKey() {
        if (secret.length() < 32) {
            throw new IllegalArgumentException("JWT密钥长度必须≥32位");
        }
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 生成Token（存储user_id，兼容原有逻辑）
    public String generateToken(String userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expire);
        return Jwts.builder()
                .setSubject(userId) // 存储数据库真实user_id
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSecretKey())
                .compact();
    }

    // 兼容原有逻辑：生成Token（存储手机号）
    public String generateTokenByPhone(String phone) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expire);
        return Jwts.builder()
                .setSubject(phone) // 存储手机号（适配旧接口）
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSecretKey())
                .compact();
    }

    // 解析Token获取user_id（核心：上传接口用）
    public String extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("Token无效或已过期");
        }
    }

    // 解析Token获取手机号（适配旧接口：修改用户资料用）
    public String extractPhone(String token) {
        return extractUserId(token); // 旧Token存的是手机号，直接复用解析逻辑
    }

    // 验证token有效性
    public boolean validateToken(String token, String phone) {
        String extractedPhone = extractPhone(token);
        return extractedPhone.equals(phone) && !isTokenExpired(token);
    }

    // 检查token是否过期
    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }
}