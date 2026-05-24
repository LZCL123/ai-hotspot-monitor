package com.example.hotspot.collector;

import com.example.hotspot.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * HackerNews 采集器。
 * 通过 Algolia 提供的 HackerNews 搜索接口拉取故事数据，并转换为系统内部的采集对象。
 */
public class HackerNewsCollector implements HotspotCollector {
    private final AppProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * 获取数据源标识
     *
     * @return 数据源名称
     */
    @Override
    public String source() {
        return "hackernews";
    }

    /**
     * 执行采集
     * 通过 Algolia HackerNews 搜索 API 拉取故事数据，最多重试配置次数。
     *
     * @param keyword 搜索关键词
     * @return 采集到的热点条目列表
     */
    @Override
    public List<CollectedItem> collect(String keyword) {
        String encoded = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = "https://hn.algolia.com/api/v1/search?query=" + encoded + "&tags=story&hitsPerPage=10";
        for (int attempt = 1; attempt <= properties.getCollector().getMaxRetries(); attempt++) {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(properties.getCollector().getTimeoutSeconds()))
                        .build();
                HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                        .timeout(Duration.ofSeconds(properties.getCollector().getTimeoutSeconds()))
                        .header("User-Agent", "ai-hotspot-monitor/0.1")
                        .GET()
                        .build();
                String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
                JsonNode hits = objectMapper.readTree(body).path("hits");
                List<CollectedItem> items = new ArrayList<>();
                for (JsonNode hit : hits) {
                    String title = text(hit, "title");
                    String itemUrl = text(hit, "url");
                    if (title.isBlank() || itemUrl.isBlank()) {
                        continue;
                    }
                    items.add(CollectedItem.builder()
                            .source(source())
                            .title(title)
                            .url(itemUrl)
                            .summary(text(hit, "story_text"))
                            .author(text(hit, "author"))
                            .publishedAt(parseTime(text(hit, "created_at")))
                            .build());
                }
                return items;
            } catch (Exception exception) {
                log.warn("HackerNews collect failed, keyword={}, attempt={}", keyword, attempt, exception);
            }
        }
        return List.of();
    }

    private String text(JsonNode node, String field) {
        return node.path(field).asText("").trim();
    }

    private LocalDateTime parseTime(String value) {
        if (value.isBlank()) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.parse(value), ZoneId.systemDefault());
    }
}
