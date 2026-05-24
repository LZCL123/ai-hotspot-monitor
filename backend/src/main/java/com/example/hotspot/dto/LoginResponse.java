package com.example.hotspot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应体。
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String role;
}
