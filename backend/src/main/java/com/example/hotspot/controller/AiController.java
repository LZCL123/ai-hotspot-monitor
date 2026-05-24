package com.example.hotspot.controller;

import com.example.hotspot.ai.AiAnalysisResult;
import com.example.hotspot.collector.CollectedItem;
import com.example.hotspot.common.ApiResponse;
import com.example.hotspot.dto.AiAnalyzeRequest;
import com.example.hotspot.dto.AiExpandRequest;
import com.example.hotspot.service.AiService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiService aiService;

    @PostMapping("/expand-keywords")
    public ApiResponse<List<String>> expand(@Valid @RequestBody AiExpandRequest request) {
        return ApiResponse.ok(aiService.expandKeywords(request.getKeyword()));
    }

    @PostMapping("/analyze")
    public ApiResponse<AiAnalysisResult> analyze(@Valid @RequestBody AiAnalyzeRequest request) {
        CollectedItem item = CollectedItem.builder()
                .title(request.getTitle())
                .summary(request.getSummary())
                .url(request.getUrl())
                .source("manual")
                .build();
        return ApiResponse.ok(aiService.analyze(request.getKeyword(), item));
    }
}
