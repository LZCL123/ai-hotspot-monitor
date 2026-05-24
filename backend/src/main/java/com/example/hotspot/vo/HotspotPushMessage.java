package com.example.hotspot.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HotspotPushMessage {
    private String event;
    private String keyword;
    private Long hotspotId;
    private String title;
    private String importance;
    private Integer relevanceScore;
    private String summary;
}
