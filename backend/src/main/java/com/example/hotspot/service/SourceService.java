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

@Service
@RequiredArgsConstructor
public class SourceService {
    private final CollectorLogMapper collectorLogMapper;
    private final AiAnalysisLogMapper aiAnalysisLogMapper;

    public List<Map<String, Object>> sources() {
        return List.of(
                Map.of("id", "hackernews", "name", "HackerNews", "enabled", true),
                Map.of("id", "bing", "name", "Bing Search", "enabled", true)
        );
    }

    public List<CollectorLog> collectorLogs() {
        return collectorLogMapper.selectList(new LambdaQueryWrapper<CollectorLog>().orderByDesc(CollectorLog::getCreatedAt).last("LIMIT 100"));
    }

    public List<AiAnalysisLog> aiLogs() {
        return aiAnalysisLogMapper.selectList(new LambdaQueryWrapper<AiAnalysisLog>().orderByDesc(AiAnalysisLog::getCreatedAt).last("LIMIT 100"));
    }
}
