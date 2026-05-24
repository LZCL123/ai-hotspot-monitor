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

/**
 * 登录认证服务。
 * 提供管理员账号密码校验、Token 生成和 Token 验证功能，支持 Redis 和本地内存两种存储方式。
 */
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

    /**
     * 用户登录
     * 校验用户名密码后生成 Token，有效期 12 小时。
     *
     * @param request 登录请求
     * @return 登录响应（Token、用户名、角色）
     * @throws BizException 用户名或密码错误时抛出
     */
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

    /**
     * 验证 Token
     * 从 Authorization 头中提取 Bearer Token 并验证有效性。
     *
     * @param authorization HTTP Authorization 请求头
     * @return 用户名
     * @throws BizException 未登录或 Token 过期时抛出
     */
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
