package com.example.hotspot.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;

/**
 * 订阅请求体。
 */
@Data
public class SubscriptionRequest {
    @NotBlank
    private String keyword;
    private List<String> sourceTypes = List.of("hackernews", "bing");
    @Min(0)
    @Max(100)
    private Integer minRelevanceScore = 60;
    @Min(5)
    private Integer intervalMinutes = 30;
    private Boolean enabled = true;
}
