package org.example.travel.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置：注册拦截器、配置静态资源等
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册JWT拦截器，拦截所有/api/**接口（除了登录、注册）
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")  // 拦截所有接口
                .excludePathPatterns("/auth/login", "/auth/register");  // 放行登录、注册接口
    }
}