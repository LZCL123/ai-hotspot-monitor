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

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private static final long DEFAULT_USER_ID = 1L;
    private final SubscriptionMapper mapper;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    public List<Subscription> list() {
        return mapper.selectList(new LambdaQueryWrapper<Subscription>().orderByDesc(Subscription::getCreatedAt));
    }

    public List<Subscription> enabledList() {
        return mapper.selectList(new LambdaQueryWrapper<Subscription>().eq(Subscription::getEnabled, true));
    }

    public Subscription create(SubscriptionRequest request) {
        Subscription entity = new Subscription();
        copy(request, entity);
        entity.setUserId(DEFAULT_USER_ID);
        entity.setExpandedKeywords(writeJson(aiService.expandKeywords(request.getKeyword())));
        mapper.insert(entity);
        return entity;
    }

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

    public void delete(Long id) {
        mapper.deleteById(id);
    }

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
