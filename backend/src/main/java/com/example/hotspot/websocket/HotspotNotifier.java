package com.example.hotspot.websocket;

import com.example.hotspot.entity.Hotspot;
import com.example.hotspot.vo.HotspotPushMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotspotNotifier {
    private final SimpMessagingTemplate messagingTemplate;

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
