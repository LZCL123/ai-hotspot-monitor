package com.example.hotspot.controller;

import com.example.hotspot.common.ApiResponse;
import com.example.hotspot.dto.LoginRequest;
import com.example.hotspot.dto.LoginResponse;
import com.example.hotspot.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录认证控制器。
 * 提供管理员登录接口，校验用户名密码后返回 Token，用于后续接口的鉴权。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * 管理员登录
     *
     * @param request 登录请求体（用户名和密码）
     * @return 登录响应（包含 Token、用户名和角色）
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}
