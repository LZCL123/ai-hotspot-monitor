package com.example.hotspot.controller;

import com.example.hotspot.common.ApiResponse;
import com.example.hotspot.dto.SubscriptionRequest;
import com.example.hotspot.entity.Subscription;
import com.example.hotspot.service.SubscriptionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService service;

    /**
     * 获取当前用户的订阅列表
     *
     * @param userId 当前用户ID（从拦截器注入）
     * @return 订阅列表
     */
    @GetMapping
    public ApiResponse<List<Subscription>> list(@RequestAttribute("userId") Long userId) {
        return ApiResponse.ok(service.list(userId));
    }

    /**
     * 创建订阅（同时通过 AiService 扩展关键字）
     *
     * @param userId  当前用户ID（从拦截器注入）
     * @param request 订阅请求体
     * @return 创建的订阅
     */
    @PostMapping
    public ApiResponse<Subscription> create(@RequestAttribute("userId") Long userId,
                                            @Valid @RequestBody SubscriptionRequest request) {
        return ApiResponse.ok(service.create(userId, request));
    }

    /**
     * 更新指定订阅
     *
     * @param userId  当前用户ID（从拦截器注入）
     * @param id      订阅ID
     * @param request 订阅请求体
     * @return 更新后的订阅
     */
    @PutMapping("/{id}")
    public ApiResponse<Subscription> update(@RequestAttribute("userId") Long userId,
                                            @PathVariable Long id,
                                            @Valid @RequestBody SubscriptionRequest request) {
        return ApiResponse.ok(service.update(userId, id, request));
    }

    /**
     * 删除订阅
     *
     * @param userId 当前用户ID（从拦截器注入）
     * @param id     订阅ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        service.delete(userId, id);
        return ApiResponse.ok(null);
    }

    /**
     * 启用订阅
     *
     * @param userId 当前用户ID（从拦截器注入）
     * @param id     订阅ID
     * @return 启用后的订阅
     */
    @PostMapping("/{id}/enable")
    public ApiResponse<Subscription> enable(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        return ApiResponse.ok(service.setEnabled(userId, id, true));
    }

    /**
     * 禁用订阅
     *
     * @param userId 当前用户ID（从拦截器注入）
     * @param id     订阅ID
     * @return 禁用后的订阅
     */
    @PostMapping("/{id}/disable")
    public ApiResponse<Subscription> disable(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        return ApiResponse.ok(service.setEnabled(userId, id, false));
    }
}
