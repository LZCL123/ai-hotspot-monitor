package com.example.hotspot.service;

import com.example.hotspot.common.BizException;
import com.example.hotspot.config.AppProperties;
import com.example.hotspot.dto.LoginRequest;
import com.example.hotspot.dto.LoginResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {
    private static final String TOKEN_PREFIX = "auth:token:";
    private final AppProperties properties;

    // 当没有 Redis 时，允许为 null 并使用本地内存回退
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    // 内存回退存储（key -> value）以及过期时间（key -> epochMilli）
    private final Map<String, String> localStore = new ConcurrentHashMap<>();
    private final Map<String, Long> localExpiry = new ConcurrentHashMap<>();

    public AuthService(AppProperties properties) {
        this.properties = properties;
    }

    public LoginResponse login(LoginRequest request) {
        if (!properties.getAuth().getUsername().equals(request.getUsername())
                || !properties.getAuth().getPassword().equals(request.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = TOKEN_PREFIX + token;
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, request.getUsername(), Duration.ofHours(12));
        } else {
            localStore.put(key, request.getUsername());
            localExpiry.put(key, Instant.now().plus(Duration.ofHours(12)).toEpochMilli());
            log.debug("Using local in-memory token store for key={}", key);
        }
        return new LoginResponse(token, request.getUsername(), "ADMIN");
    }

    public String requireToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BizException(401, "请先登录");
        }
        String token = authorization.substring("Bearer ".length());
        String key = TOKEN_PREFIX + token;
        String username = null;
        if (redisTemplate != null) {
            username = redisTemplate.opsForValue().get(key);
        } else {
            Long exp = localExpiry.get(key);
            if (exp != null && Instant.now().toEpochMilli() > exp) {
                // expired
                localStore.remove(key);
                localExpiry.remove(key);
                username = null;
            } else {
                username = localStore.get(key);
            }
        }
        if (username == null) {
            throw new BizException(401, "登录已过期");
        }
        return username;
    }
}
