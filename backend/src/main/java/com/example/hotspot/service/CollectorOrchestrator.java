package com.example.hotspot.service;

import com.example.hotspot.ai.AiAnalysisResult;
import com.example.hotspot.collector.CollectedItem;
import com.example.hotspot.collector.HotspotCollector;
import com.example.hotspot.entity.CollectorLog;
import com.example.hotspot.entity.Hotspot;
import com.example.hotspot.entity.Subscription;
import com.example.hotspot.mapper.CollectorLogMapper;
import com.example.hotspot.websocket.HotspotNotifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 采集编排服务。
 * 管理所有数据源的采集流程：遍历已启用的订阅，执行采集、AI 分析、去重、入库和 WebSocket 推送。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorOrchestrator {
    private final List<HotspotCollector> collectorList;
    private final SubscriptionService subscriptionService;
    private final HotspotService hotspotService;
    private final AiService aiService;
    private final CollectorLogMapper collectorLogMapper;
    private final HotspotNotifier notifier;
    private final ObjectMapper objectMapper;

    /**
     * 刷新所有订阅
     * 遍历全部启用的订阅，分别对其配置的数据源执行采集和分析流程。
     *
     * @return 本次新增的热点总数
     */
    public int refreshAll() {
        int created = 0;
        Map<String, HotspotCollector> collectors = collectorList.stream()
                .collect(Collectors.toMap(HotspotCollector::source, Function.identity()));
        for (Subscription subscription : subscriptionService.enabledList()) {
            for (String source : readSources(subscription.getSourceTypes())) {
                HotspotCollector collector = collectors.get(source);
                if (collector != null) {
                    created += collectSubscription(subscription, collector);
                }
            }
        }
        return created;
    }

    /**
     * 定时调度刷新
     */
    @Scheduled(fixedDelayString = "${COLLECTOR_FIXED_DELAY_MS:300000}")
    public void scheduledRefresh() {
        try {
            refreshAll();
        } catch (Exception exception) {
            log.warn("Scheduled refresh failed", exception);
        }
    }

    private int collectSubscription(Subscription subscription, HotspotCollector collector) {
        CollectorLog logRow = new CollectorLog();
        logRow.setSource(collector.source());
        logRow.setKeyword(subscription.getKeyword());
        logRow.setStatus("SUCCESS");
        logRow.setStartedAt(LocalDateTime.now());
        int created = 0;
        try {
            List<CollectedItem> items = collector.collect(subscription.getKeyword());
            for (CollectedItem item : items) {
                if (hotspotService.existsByUrl(item.getUrl()) || hotspotService.existsSimilarTitle(subscription.getKeyword(), item.getTitle())) {
                    continue;
                }
                AiAnalysisResult analysis = aiService.analyze(subscription.getKeyword(), item);
                if (!analysis.isValid() || analysis.getRelevanceScore() < subscription.getMinRelevanceScore()) {
                    continue;
                }
                Hotspot hotspot = toHotspot(subscription.getKeyword(), item, analysis);
                hotspotService.insert(hotspot);
                notifier.notifyNew(hotspot);
                created++;
            }
            logRow.setMessage("collected=" + items.size() + ", created=" + created);
        } catch (Exception exception) {
            logRow.setStatus("FAILED");
            logRow.setMessage(exception.getMessage());
            log.warn("Collect subscription failed, source={}, keyword={}", collector.source(), subscription.getKeyword(), exception);
        } finally {
            logRow.setFinishedAt(LocalDateTime.now());
            collectorLogMapper.insert(logRow);
        }
        return created;
    }

    @SneakyThrows
    private Hotspot toHotspot(String keyword, CollectedItem item, AiAnalysisResult analysis) {
        Hotspot hotspot = new Hotspot();
        hotspot.setTitle(item.getTitle());
        hotspot.setUrl(item.getUrl());
        hotspot.setSource(item.getSource());
        hotspot.setKeyword(keyword);
        hotspot.setSummary(analysis.getSummary());
        hotspot.setRawSummary(item.getSummary());
        hotspot.setAuthor(item.getAuthor());
        hotspot.setRelevanceScore(analysis.getRelevanceScore());
        hotspot.setImportance(analysis.getImportance());
        hotspot.setEventType(analysis.getEventType());
        hotspot.setTags(objectMapper.writeValueAsString(analysis.getTags()));
        hotspot.setPublishedAt(item.getPublishedAt());
        return hotspot;
    }

    @SneakyThrows
    private List<String> readSources(String json) {
        if (json == null || json.isBlank()) {
            return List.of("hackernews", "bing");
        }
        return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
    }
}
