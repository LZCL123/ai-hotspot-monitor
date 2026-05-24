package com.example.hotspot.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 热点引用信息。
 * 在智能问答中作为 AI 回答的参考来源，包含标题、链接、摘要和相关性评分。
 */
@Data
@AllArgsConstructor
public class AgentReference {
    private Long id;
    private String title;
    private String url;
    private String source;
    private String keyword;
    private String summary;
    private Integer relevanceScore;
}
