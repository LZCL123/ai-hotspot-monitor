package com.example.hotspot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hotspot.common.BizException;
import com.example.hotspot.dto.ChangePasswordRequest;
import com.example.hotspot.dto.LoginRequest;
import com.example.hotspot.dto.LoginResponse;
import com.example.hotspot.dto.RegisterRequest;
import com.example.hotspot.entity.User;
import com.example.hotspot.mapper.UserMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户认证服务。
 * 支持多用户注册、登录、修改密码，基于数据库存储 + BCrypt 密码加密 + Redis/本地双缓存 Token。
 */
@Service
@Slf4j
public class AuthService {
    private static final String TOKEN_PREFIX = "auth:token:";
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    private final Map<String, String> localStore = new ConcurrentHashMap<>();
    private final Map<String, Long> localExpiry = new ConcurrentHashMap<>();

    public AuthService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 初始化默认管理员账户
     * 系统启动时检查是否存在 admin 用户，不存在则自动创建。
     */
    @PostConstruct
    public void initAdmin() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, "admin");
        if (userMapper.selectCount(wrapper) == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userMapper.insert(admin);
            log.info("Default admin user created: admin / admin123");
        }
    }

    /**
     * 用户注册
     * 检查用户名是否已存在，不存在则插入数据库并返回 Token。
     *
     * @param request 注册请求
     * @return 登录响应
     * @throws BizException 用户名已存在时抛出
     */
    public LoginResponse register(RegisterRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BizException(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        userMapper.insert(user);

        return loginInternal(user.getUsername());
    }

    /**
     * 用户登录
     * 校验用户名密码后生成 Token，有效期 12 小时。
     *
     * @param request 登录请求
     * @return 登录响应
     * @throws BizException 用户名或密码错误时抛出
     */
    public LoginResponse login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BizException(401, "用户名或密码错误");
        }

        return loginInternal(user.getUsername());
    }

    /**
     * 修改密码
     * 验证旧密码后更新新密码。
     *
     * @param username 当前用户名
     * @param request 修改密码请求
     * @throws BizException 旧密码错误或新密码格式错误时抛出
     */
    public void changePassword(String username, ChangePasswordRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null || !passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BizException(401, "旧密码错误");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    /**
     * 获取当前用户信息
     *
     * @param username 用户名
     * @return 用户实体
     * @throws BizException 用户不存在时抛出
     */
    public User getUserInfo(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return user;
    }

    /**
     * 内部方法：登录成功后生成 Token 并存储
     */
    private LoginResponse loginInternal(String username) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String key = buildTokenKey(token);
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, username, Duration.ofHours(12));
        }
        localStore.put(key, username);
        localExpiry.put(key, Instant.now().plus(Duration.ofHours(12)).toEpochMilli());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        return new LoginResponse(token, username, user.getRole());
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
        String token = extractToken(authorization);
        String key = buildTokenKey(token);
        String username = null;
        // 先查 Redis
        if (redisTemplate != null) {
            username = redisTemplate.opsForValue().get(key);
        }
        // Redis 没查到或 Redis 挂了，走本地内存兜底
        if (username == null) {
            Long exp = localExpiry.get(key);
            if (exp != null && Instant.now().toEpochMilli() > exp) {
                localStore.remove(key);
                localExpiry.remove(key);
            } else {
                username = localStore.get(key);
            }
        }
        if (username == null) {
            throw new BizException(401, "登录已过期");
        }
        return username;
    }

    /**
     * 从 Authorization 头中提取 Bearer Token
     */
    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BizException(401, "请先登录");
        }
        return authorization.substring("Bearer ".length());
    }

    /**
     * 构建 Token 在存储中的 key
     */
    private String buildTokenKey(String token) {
        return TOKEN_PREFIX + token;
    }
}
