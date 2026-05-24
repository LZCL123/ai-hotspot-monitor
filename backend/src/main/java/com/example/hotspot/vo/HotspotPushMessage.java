package com.example.hotspot.vo;

import lombok.Builder;
import lombok.Data;

/**
 * WebSocket 热点推送消息。
 * 当采集到新的热点时，通过 STOMP 协议推送给前端订阅者。
 */
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
