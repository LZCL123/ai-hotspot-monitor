package com.example.hotspot.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

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
