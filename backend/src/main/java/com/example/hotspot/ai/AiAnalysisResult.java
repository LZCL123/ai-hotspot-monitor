package com.example.hotspot.ai;

import java.util.List;
import lombok.Data;

@Data
/**
 * AI 分析结果模型。
 * 承载模型或本地规则对单条热点的判断结果，包括相关性、重要性、摘要、标签和事件类型。
 */
public class AiAnalysisResult {
    private boolean valid = true;
    private int relevanceScore;
    private String importance;
    private String summary;
    private String reason;
    private List<String> tags;
    private String eventType;
}
