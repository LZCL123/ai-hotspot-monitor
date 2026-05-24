package com.example.hotspot.collector;

import com.example.hotspot.config.AppProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * Bing 搜索采集器。
 * 根据订阅关键词从 Bing 搜索结果页抓取候选热点，解析标题、链接和摘要后交给后续 AI 分析流程。
 */
public class BingSearchCollector implements HotspotCollector {
    private final AppProperties properties;

    @Override
    public String source() {
        return "bing";
    }

    @Override
    public List<CollectedItem> collect(String keyword) {
        String url = "https://www.bing.com/search?q=" + URLEncoder.encode(keyword + " AI", StandardCharsets.UTF_8);
        for (int attempt = 1; attempt <= properties.getCollector().getMaxRetries(); attempt++) {
            try {
                Document doc = Jsoup.connect(url)
                        .userAgent("ai-hotspot-monitor/0.1")
                        .timeout(properties.getCollector().getTimeoutSeconds() * 1000)
                        .get();
                return doc.select("li.b_algo").stream()
                        .limit(10)
                        .map(element -> {
                            var link = element.selectFirst("h2 a");
                            String title = link == null ? "" : link.text();
                            String href = link == null ? "" : link.attr("href");
                            String summary = element.select(".b_caption p").text();
                            return CollectedItem.builder()
                                    .source(source())
                                    .title(title)
                                    .url(href)
                                    .summary(summary)
                                    .build();
                        })
                        .filter(item -> item.getTitle() != null && !item.getTitle().isBlank())
                        .filter(item -> item.getUrl() != null && item.getUrl().startsWith("http"))
                        .toList();
            } catch (Exception exception) {
                log.warn("Bing collect failed, keyword={}, attempt={}", keyword, attempt, exception);
            }
        }
        return List.of();
    }
}
