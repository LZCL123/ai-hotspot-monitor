package com.example.hotspot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 关键词扩展请求体。
 */
@Data
public class AiExpandRequest {
    @NotBlank
    private String keyword;
}
