package org.example.travel.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.travel.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

/**
 * JWT工具类
 */
@Component
public class JwtTokenUtil {
    @Value("${jwt.secret}")
    private String secret;      // JWT密钥

    // ========== 关键修改：把expiration改成expire ==========
    @Value("${jwt.expire}")
    private long expiration;    // 过期时间（毫秒）

    /**
     * 生成Token
     */
    public String generateToken(String userId, String phone) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("phone", phone)
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * 从Token解析用户ID
     */
    public String getUserIdFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody()
                    .get("userId", String.class);
        } catch (Exception e) {
            throw new BusinessException("Token无效或已过期");
        }
    }
}