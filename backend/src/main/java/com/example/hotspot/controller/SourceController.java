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

@RestController
@RequiredArgsConstructor
public class SourceController {
    private final SourceService sourceService;

    @GetMapping("/api/sources")
    public ApiResponse<List<Map<String, Object>>> sources() {
        return ApiResponse.ok(sourceService.sources());
    }

    @GetMapping("/api/collector-logs")
    public ApiResponse<List<CollectorLog>> collectorLogs() {
        return ApiResponse.ok(sourceService.collectorLogs());
    }

    @GetMapping("/api/ai-logs")
    public ApiResponse<List<AiAnalysisLog>> aiLogs() {
        return ApiResponse.ok(sourceService.aiLogs());
    }
}
