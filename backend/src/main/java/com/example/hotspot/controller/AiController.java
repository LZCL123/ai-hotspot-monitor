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

/**
 * AI 分析与扩展控制器。
 * 提供关键词扩展和热点分析接口，对接 AI 服务进行语义理解和相关性评估。
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiService aiService;

    /**
     * 扩展关键词
     * 根据用户输入的关键词生成一组相关的扩展关键词，用于多维度采��。
     *
     * @param request 关键词请求体
     * @return 扩展后的关键词列表
     */
    @PostMapping("/expand-keywords")
    public ApiResponse<List<String>> expand(@Valid @RequestBody AiExpandRequest request) {
        return ApiResponse.ok(aiService.expandKeywords(request.getKeyword()));
    }

    /**
     * 热点分析
     * 对指定的关键词和文章内容进行 AI 分析，评估相关性、重要性和事件类型。
     *
     * @param request 分析请求体（包含关键词和文章信息）
     * @return AI 分析结果
     */
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
