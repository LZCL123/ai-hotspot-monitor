package com.example.hotspot.websocket;

import com.example.hotspot.entity.Hotspot;
import com.example.hotspot.vo.HotspotPushMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 热点 WebSocket 推送服务。
 * 采集到新热点后，通过 STOMP 协议向订阅了相关关键词或全部热点的前端推送实时消息。
 */
@Component
@RequiredArgsConstructor
public class HotspotNotifier {
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 推送新热点
     * 同时向单个关键词频道和全局频道发送通知。
     *
     * @param hotspot 新入库的热点对象
     */
    public void notifyNew(Hotspot hotspot) {
        HotspotPushMessage message = HotspotPushMessage.builder()
                .event("hotspot:new")
                .keyword(hotspot.getKeyword())
                .hotspotId(hotspot.getId())
                .title(hotspot.getTitle())
                .importance(hotspot.getImportance())
                .relevanceScore(hotspot.getRelevanceScore())
                .summary(hotspot.getSummary())
                .build();
        messagingTemplate.convertAndSend("/topic/hotspots/" + hotspot.getKeyword(), message);
        messagingTemplate.convertAndSend("/topic/hotspots", message);
    }
}
