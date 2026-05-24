package com.example.hotspot.config;

import com.example.hotspot.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
/**
 * Web MVC 配置。
 * 给 /api 接口注册登录拦截器，除登录接口和 OPTIONS 请求外都要求携带有效 Token。
 */
public class WebMvcConfig implements WebMvcConfigurer {
    private final AuthService authService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || request.getRequestURI().startsWith("/api/auth")) {
                    return true;
                }
                authService.requireToken(request.getHeader("Authorization"));
                return true;
            }
        }).addPathPatterns("/api/**");
    }
}
