package com.example.hotspot.controller;

import com.example.hotspot.common.ApiResponse;
import com.example.hotspot.dto.ChangePasswordRequest;
import com.example.hotspot.dto.LoginRequest;
import com.example.hotspot.dto.LoginResponse;
import com.example.hotspot.dto.RegisterRequest;
import com.example.hotspot.entity.User;
import com.example.hotspot.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证控制器。
 * 提供用户注册、登录、修改密码等接口。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * 用户注册
     *
     * @param request 注册请求体（用户名和密码）
     * @return 登录响应（包含 Token、用户名和角色）
     */
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    /**
     * 用户登录
     *
     * @param request 登录请求体（用户名和密码）
     * @return 登录响应（包含 Token、用户名和角色）
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    /**
     * 修改密码
     *
     * @param username 当前用户名（从 Token 中获取）
     * @param request 修改密码请求体（旧密码和新密码）
     * @return 操作结果
     */
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@RequestAttribute("username") String username,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(username, request);
        return ApiResponse.ok(null);
    }

    /**
     * 获取当前用户信息
     *
     * @param username 当前用户名（从 Token 中获取）
     * @return 用户信息
     */
    @GetMapping("/me")
    public ApiResponse<User> me(@RequestAttribute("username") String username) {
        return ApiResponse.ok(authService.getUserInfo(username));
    }
}
