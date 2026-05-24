package com.example.hotspot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hotspot.entity.AiAnalysisLog;
import com.example.hotspot.entity.CollectorLog;
import com.example.hotspot.mapper.AiAnalysisLogMapper;
import com.example.hotspot.mapper.CollectorLogMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 数据源和日志查询服务。
 * 提供已注册采集源的信息查询，以及采集日志和 AI 分析日志的查询能力。
 */
@Service
@RequiredArgsConstructor
public class SourceService {
    private final CollectorLogMapper collectorLogMapper;
    private final AiAnalysisLogMapper aiAnalysisLogMapper;

    /**
     * 获取数据源列表
     *
     * @return 数据源信息列表
     */
    public List<Map<String, Object>> sources() {
        return List.of(
                Map.of("id", "hackernews", "name", "HackerNews", "enabled", true),
                Map.of("id", "bing", "name", "Bing Search", "enabled", true)
        );
    }

    /**
     * 获取最近采集日志
     *
     * @return 最近 100 条采集日志
     */
    public List<CollectorLog> collectorLogs() {
        return collectorLogMapper.selectList(new LambdaQueryWrapper<CollectorLog>().orderByDesc(CollectorLog::getCreatedAt).last("LIMIT 100"));
    }

    /**
     * 获取最近 AI 分析日志
     *
     * @return 最近 100 条 AI 分析日志
     */
    public List<AiAnalysisLog> aiLogs() {
        return aiAnalysisLogMapper.selectList(new LambdaQueryWrapper<AiAnalysisLog>().orderByDesc(AiAnalysisLog::getCreatedAt).last("LIMIT 100"));
    }
}
