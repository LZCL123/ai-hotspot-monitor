package com.example.hotspot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hotspot.common.BizException;
import com.example.hotspot.dto.SubscriptionRequest;
import com.example.hotspot.entity.Subscription;
import com.example.hotspot.mapper.SubscriptionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

/**
 * 订阅管理服务。
 * 提供订阅关键词的增删改查、启用禁用管理，以及创建/更新时自动扩展关键词。
 * 支持多用户数据隔离，所有操作都基于 userId。
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionMapper mapper;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    /**
     * 获取当前用户的全部订阅列表
     *
     * @param userId 用户ID
     * @return 订阅列表（按创建时间降序）
     */
    public List<Subscription> list(Long userId) {
        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscription::getUserId, userId);
        wrapper.orderByDesc(Subscription::getCreatedAt);
        return mapper.selectList(wrapper);
    }

    /**
     * 获取当前用户已启用的订阅列表
     *
     * @param userId 用户ID
     * @return 已启用的订阅列表
     */
    public List<Subscription> enabledList(Long userId) {
        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscription::getUserId, userId);
        wrapper.eq(Subscription::getEnabled, true);
        return mapper.selectList(wrapper);
    }

    /**
     * 获取全部已启用的订阅列表（系统内部使用）
     * 供采集编排器遍历所有用户的订阅进行采集，不做用户隔离。
     *
     * @return 全部已启用的订阅列表
     */
    public List<Subscription> enabledList() {
        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscription::getEnabled, true);
        return mapper.selectList(wrapper);
    }

    /**
     * 创建订阅
     * 自动扩展关键词并保存到数据库。
     *
     * @param userId  用户ID
     * @param request 订阅请求
     * @return 创建的订阅
     */
    public Subscription create(Long userId, SubscriptionRequest request) {
        Subscription entity = new Subscription();
        copy(request, entity);
        entity.setUserId(userId);
        entity.setExpandedKeywords(writeJson(aiService.expandKeywords(request.getKeyword())));
        mapper.insert(entity);
        return entity;
    }

    /**
     * 更新订阅
     * 重新扩展关键词并更新数据库记录。
     *
     * @param userId  用户ID
     * @param id      订阅ID
     * @param request 订阅请求
     * @return 更新后的订阅
     * @throws BizException 订阅不存在或不属于当前用户时抛出
     */
    public Subscription update(Long userId, Long id, SubscriptionRequest request) {
        Subscription entity = mapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new BizException("订阅不存在");
        }
        copy(request, entity);
        entity.setExpandedKeywords(writeJson(aiService.expandKeywords(request.getKeyword())));
        mapper.updateById(entity);
        return mapper.selectById(id);
    }

    /**
     * 删除订阅
     *
     * @param userId 用户ID
     * @param id     订阅ID
     */
    public void delete(Long userId, Long id) {
        Subscription entity = mapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new BizException("订阅不存在");
        }
        mapper.deleteById(id);
    }

    /**
     * 设置订阅启用/禁用状态
     *
     * @param userId  用户ID
     * @param id      订阅ID
     * @param enabled 启用状态
     * @return 更新后的订阅
     * @throws BizException 订阅不存在或不属于当前用户时抛出
     */
    public Subscription setEnabled(Long userId, Long id, boolean enabled) {
        Subscription entity = mapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new BizException("订阅不存在");
        }
        entity.setEnabled(enabled);
        mapper.updateById(entity);
        return mapper.selectById(id);
    }

    private void copy(SubscriptionRequest request, Subscription entity) {
        entity.setKeyword(request.getKeyword());
        entity.setSourceTypes(writeJson(request.getSourceTypes()));
        entity.setMinRelevanceScore(request.getMinRelevanceScore());
        entity.setIntervalMinutes(request.getIntervalMinutes());
        entity.setEnabled(request.getEnabled());
    }

    @SneakyThrows
    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}
