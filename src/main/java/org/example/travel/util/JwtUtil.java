package org.example.travel.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT工具类：生成token、验证token、解析token中的用户信息
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;  // JWT密钥（从配置文件读取）

    @Value("${jwt.expire}")
    private long expire;    // token有效期（毫秒）

    // 生成token（基于用户手机号）
    public String generateToken(String phone) {
        // 密钥处理：确保密钥长度足够（JWT要求HS256密钥至少256位）
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());

        return Jwts.builder()
                .setSubject(phone)  // token中存储的核心信息（用户手机号）
                .setIssuedAt(new Date())  // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expire))  // 过期时间
                .signWith(key)  // 签名
                .compact();
    }

    // 验证token有效性（是否过期、签名是否正确）
    public boolean validateToken(String token, String phone) {
        String extractedPhone = extractPhone(token);
        return extractedPhone.equals(phone) && !isTokenExpired(token);
    }

    // 从token中解析出用户手机号
    public String extractPhone(String token) {
        return extractClaims(token).getSubject();
    }

    // 解析token中的所有声明（Claims）
    private Claims extractClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 检查token是否过期
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}