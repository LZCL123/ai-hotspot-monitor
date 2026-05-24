package com.example.hotspot.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
/**
 * API 请求日志过滤器。
 * 记录每个 /api 请求的方法、路径、状态码、耗时和来源地址，便于排查接口访问问题。
 */
public class RequestLoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (request.getRequestURI().startsWith("/api/")) {
                log.info("{} {} -> {} ({} ms) from {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        System.currentTimeMillis() - start,
                        request.getRemoteAddr());
            }
        }
    }
}
