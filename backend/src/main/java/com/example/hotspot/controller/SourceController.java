package com.example.hotspot.controller;

import com.example.hotspot.common.ApiResponse;
import com.example.hotspot.entity.AiAnalysisLog;
import com.example.hotspot.entity.CollectorLog;
import com.example.hotspot.service.SourceService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据源和日志控制器。
 * 提供已注册的数据源列表以及采集和 AI 分析的历史日志查询。
 */
@RestController
@RequiredArgsConstructor
public class SourceController {
    private final SourceService sourceService;

    /**
     * 获取数据源列表
     *
     * @return 数据源信息列表（包含标识、名称和启用状态）
     */
    @GetMapping("/api/sources")
    public ApiResponse<List<Map<String, Object>>> sources() {
        return ApiResponse.ok(sourceService.sources());
    }

    /**
     * 获取采集日志
     *
     * @return 最近 100 条采集操作日志
     */
    @GetMapping("/api/collector-logs")
    public ApiResponse<List<CollectorLog>> collectorLogs() {
        return ApiResponse.ok(sourceService.collectorLogs());
    }

    /**
     * 获取 AI 分析日志
     *
     * @return 最近 100 条 AI 分析日志
     */
    @GetMapping("/api/ai-logs")
    public ApiResponse<List<AiAnalysisLog>> aiLogs() {
        return ApiResponse.ok(sourceService.aiLogs());
    }
}
