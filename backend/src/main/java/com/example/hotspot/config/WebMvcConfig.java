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
 * 注册登录拦截器拦截 /api 接口，通过 excludePathPatterns 放行无需认证的路径。
 */
public class WebMvcConfig implements WebMvcConfigurer {
    private final AuthService authService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                String username = authService.requireToken(request.getHeader("Authorization"));
                request.setAttribute("username", username);
                // 缓存 userId 到 request attribute，避免每次查询
                com.example.hotspot.entity.User user = authService.getUserInfo(username);
                request.setAttribute("userId", user.getId());
                return true;
            }
        })
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**");
    }
}
