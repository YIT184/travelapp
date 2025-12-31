package org.example.travel.config;

import org.example.travel.exception.BusinessException;
import org.example.travel.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT拦截器：对需要认证的接口进行token验证
 */
@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头获取token（格式：Authorization: Bearer {token}）
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(401, "请先登录");
        }

        // 2. 提取token
        String token = authHeader.substring(7);

        try {
            // 3. 验证token是否过期（解析成功则未过期）
            jwtUtil.extractUserId(token);
        } catch (Exception e) {
            log.error("token验证失败：", e);
            throw new BusinessException(401, "登录已过期，请重新登录");
        }

        // 4. token有效，放行
        return true;
    }
}