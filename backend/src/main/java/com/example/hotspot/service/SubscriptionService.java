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
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private static final long DEFAULT_USER_ID = 1L;
    private final SubscriptionMapper mapper;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    /**
     * 获取全部订阅列表
     *
     * @return 订阅列表（按创建时间降序）
     */
    public List<Subscription> list() {
        return mapper.selectList(new LambdaQueryWrapper<Subscription>().orderByDesc(Subscription::getCreatedAt));
    }

    /**
     * 获取已启用的订阅列表
     *
     * @return 已启用的订阅列表
     */
    public List<Subscription> enabledList() {
        return mapper.selectList(new LambdaQueryWrapper<Subscription>().eq(Subscription::getEnabled, true));
    }

    /**
     * 创建订阅
     * 自动扩展关键词并保存到数据库。
     *
     * @param request 订阅请求
     * @return 创建的订阅
     */
    public Subscription create(SubscriptionRequest request) {
        Subscription entity = new Subscription();
        copy(request, entity);
        entity.setUserId(DEFAULT_USER_ID);
        entity.setExpandedKeywords(writeJson(aiService.expandKeywords(request.getKeyword())));
        mapper.insert(entity);
        return entity;
    }

    /**
     * 更新订阅
     * 重新扩展关键词并更新数据库记录。
     *
     * @param id      订阅ID
     * @param request 订阅请求
     * @return 更新后的订阅
     * @throws BizException 订阅不存在时抛出
     */
    public Subscription update(Long id, SubscriptionRequest request) {
        Subscription entity = mapper.selectById(id);
        if (entity == null) {
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
     * @param id 订阅ID
     */
    public void delete(Long id) {
        mapper.deleteById(id);
    }

    /**
     * 设置订阅启用/禁用状态
     *
     * @param id      订阅ID
     * @param enabled 启用状态
     * @return 更新后的订阅
     * @throws BizException 订阅不存在时抛出
     */
    public Subscription setEnabled(Long id, boolean enabled) {
        Subscription entity = mapper.selectById(id);
        if (entity == null) {
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
