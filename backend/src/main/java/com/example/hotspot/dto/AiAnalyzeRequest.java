package com.example.hotspot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 热点分析请求体。
 */
@Data
public class AiAnalyzeRequest {
    @NotBlank
    private String keyword;
    @NotBlank
    private String title;
    private String summary;
    private String url;
}
