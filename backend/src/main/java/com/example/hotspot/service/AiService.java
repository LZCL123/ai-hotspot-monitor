package com.example.hotspot.service;

import com.example.hotspot.ai.AiAnalysisResult;
import com.example.hotspot.collector.CollectedItem;
import com.example.hotspot.config.AppProperties;
import com.example.hotspot.entity.AiAnalysisLog;
import com.example.hotspot.mapper.AiAnalysisLogMapper;
import com.example.hotspot.vo.AgentReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {
    private final AppProperties properties;
    private final ObjectMapper objectMapper;
    private final AiAnalysisLogMapper aiAnalysisLogMapper;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    // 内存回退缓存
    // Fallback cache used only when Redis is not available.
    private final Map<String, String> localCache = new ConcurrentHashMap<>();
    private final Map<String, Long> localExpiry = new ConcurrentHashMap<>();

    public List<String> expandKeywords(String keyword) {
        String cacheKey = "ai:expand:" + keyword.toLowerCase();
        String cached = getValue(cacheKey);
        if (StringUtils.hasText(cached)) {
            return readList(cached);
        }
        List<String> expanded = new java.util.ArrayList<>(new LinkedHashSet<>(List.of(
                keyword,
                keyword + " news",
                keyword + " launch",
                keyword + " funding",
                keyword + " open source",
                keyword + " research",
                keyword + " product"
        )));
        setValue(cacheKey, writeJson(expanded), Duration.ofHours(24));
        return expanded;
    }

    public AiAnalysisResult analyze(String keyword, CollectedItem item) {
        // Use a stable hash so the same keyword + article does not call the model repeatedly.
        String input = keyword + "|" + item.getTitle() + "|" + item.getUrl() + "|" + item.getSummary();
        String hash = sha256(input);
        String cached = getValue("ai:analysis:" + hash);
        if (StringUtils.hasText(cached)) {
            return read(cached, AiAnalysisResult.class);
        }
        // With DASHSCOPE_API_KEY set, call Bailian. Without a key, use local rules.
        AiAnalysisResult result = StringUtils.hasText(activeApiKey())
                ? callOpenAiCompatible(keyword, item)
                : localAnalyze(keyword, item);
        String json = writeJson(result);
        setValue("ai:analysis:" + hash, json, Duration.ofDays(7));
        AiAnalysisLog logRow = new AiAnalysisLog();
        logRow.setProvider(StringUtils.hasText(activeApiKey()) ? activeProvider() : "local-rule");
        logRow.setModel(StringUtils.hasText(activeApiKey()) ? activeModel() : "fallback");
        logRow.setInputHash(hash);
        logRow.setOutputJson(json);
        logRow.setCostTokens(0);
        try {
            aiAnalysisLogMapper.insert(logRow);
        } catch (Exception ignored) {
            log.debug("AI analysis log already exists for hash={}", hash);
        }
        return result;
    }

    public String answerWithReferences(String question, List<AgentReference> references) {
        if (!StringUtils.hasText(activeApiKey())) {
            return localAgentAnswer(question, references);
        }
        try {
            String prompt = buildAgentPrompt(question, references);
            Map<String, Object> payload = Map.of(
                    "model", activeModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", "你是 AI 热点监控工具里的智能客服。只基于给定参考资料回答，资料不足时要明确说明。回答使用简体中文，语气简洁、专业。"),
                            Map.of("role", "user", "content", prompt)
                    )
            );
            HttpRequest request = HttpRequest.newBuilder(URI.create(activeBaseUrl()))
                    .timeout(Duration.ofSeconds(25))
                    .header("Authorization", "Bearer " + activeApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(writeJson(payload)))
                    .build();
            String body = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).body();
            JsonNode root = objectMapper.readTree(body);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            return StringUtils.hasText(content) ? content : localAgentAnswer(question, references);
        } catch (Exception exception) {
            log.warn("{} agent chat failed, fallback to local answer", activeProvider(), exception);
            return localAgentAnswer(question, references);
        }
    }

    private String buildAgentPrompt(String question, List<AgentReference> references) {
        StringBuilder builder = new StringBuilder();
        builder.append("用户问题：").append(question).append("\n\n参考资料：\n");
        if (references.isEmpty()) {
            builder.append("无匹配资料。\n");
        }
        for (int i = 0; i < references.size(); i++) {
            AgentReference reference = references.get(i);
            builder.append(i + 1).append(". ")
                    .append("标题：").append(reference.getTitle()).append("\n")
                    .append("关键词：").append(reference.getKeyword()).append("\n")
                    .append("来源：").append(reference.getSource()).append("\n")
                    .append("摘要：").append(reference.getSummary()).append("\n")
                    .append("链接：").append(reference.getUrl()).append("\n\n");
        }
        builder.append("请给出直接回答，并在最后用“参考：”列出用到的标题。");
        return builder.toString();
    }

    private String localAgentAnswer(String question, List<AgentReference> references) {
        if (references.isEmpty()) {
            return "我暂时没有在已采集热点里找到足够相关的资料。可以先新增或刷新订阅关键词，再重新提问。";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("根据当前已采集热点，和“").append(question).append("”最相关的信息有：\n");
        for (int i = 0; i < references.size(); i++) {
            AgentReference reference = references.get(i);
            builder.append(i + 1).append(". ")
                    .append(reference.getTitle());
            if (StringUtils.hasText(reference.getSummary())) {
                builder.append("：").append(reference.getSummary());
            }
            builder.append("\n");
        }
        builder.append("建议优先查看相关度较高、来源更可信的条目，再结合原文链接判断。");
        return builder.toString();
    }

    private AiAnalysisResult callOpenAiCompatible(String keyword, CollectedItem item) {
        try {
            // Bailian supports the OpenAI chat completions shape.
            String prompt = """
                    你是 AI 热点监控分析器。请只输出 JSON，字段必须包含：
                    isValid(boolean), relevanceScore(0-100), importance(LOW/MEDIUM/HIGH/CRITICAL),
                    summary(中文1-3句), reason, tags(字符串数组), eventType。
                    关键词：%s
                    标题：%s
                    摘要：%s
                    链接：%s
                    """.formatted(keyword, item.getTitle(), item.getSummary(), item.getUrl());
            Map<String, Object> payload = Map.of(
                    "model", activeModel(),
                    "response_format", Map.of("type", "json_object"),
                    "messages", List.of(Map.of("role", "user", "content", prompt))
            );
            HttpRequest request = HttpRequest.newBuilder(URI.create(activeBaseUrl()))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + activeApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(writeJson(payload)))
                    .build();
            String body = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).body();
            JsonNode root = objectMapper.readTree(body);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            JsonNode json = objectMapper.readTree(content);
            AiAnalysisResult result = new AiAnalysisResult();
            result.setValid(json.path("isValid").asBoolean(true));
            result.setRelevanceScore(json.path("relevanceScore").asInt(60));
            result.setImportance(json.path("importance").asText("MEDIUM"));
            result.setSummary(json.path("summary").asText(item.getSummary()));
            result.setReason(json.path("reason").asText(""));
            result.setTags(readList(json.path("tags").toString()));
            result.setEventType(json.path("eventType").asText("行业趋势"));
            return result;
        } catch (Exception exception) {
            log.warn("{} analyze failed, fallback to local rules", activeProvider(), exception);
            return localAnalyze(keyword, item);
        }
    }

    private String activeProvider() {
        // Prefer the new app.ai block, while keeping old OpenRouter config usable.
        if (usePrimaryAiConfig() && StringUtils.hasText(properties.getAi().getProvider())) {
            return properties.getAi().getProvider();
        }
        return "openrouter";
    }

    private String activeApiKey() {
        if (StringUtils.hasText(properties.getAi().getApiKey())) {
            return properties.getAi().getApiKey();
        }
        return properties.getOpenrouter().getApiKey();
    }

    private String activeModel() {
        if (usePrimaryAiConfig() && StringUtils.hasText(properties.getAi().getModel())) {
            return properties.getAi().getModel();
        }
        return properties.getOpenrouter().getModel();
    }

    private String activeBaseUrl() {
        if (usePrimaryAiConfig() && StringUtils.hasText(properties.getAi().getBaseUrl())) {
            return properties.getAi().getBaseUrl();
        }
        return properties.getOpenrouter().getBaseUrl();
    }

    private boolean usePrimaryAiConfig() {
        return StringUtils.hasText(properties.getAi().getApiKey())
                || !StringUtils.hasText(properties.getOpenrouter().getApiKey());
    }

    private AiAnalysisResult localAnalyze(String keyword, CollectedItem item) {
        // Local fallback keeps the app usable if no model key is configured or the provider fails.
        String text = (item.getTitle() + " " + item.getSummary()).toLowerCase();
        int score = text.contains(keyword.toLowerCase()) ? 82 : 58;
        if (text.contains("launch") || text.contains("release") || text.contains("funding")) {
            score += 8;
        }
        score = Math.min(100, score);
        AiAnalysisResult result = new AiAnalysisResult();
        result.setValid(true);
        result.setRelevanceScore(score);
        result.setImportance(score >= 90 ? "CRITICAL" : score >= 80 ? "HIGH" : score >= 65 ? "MEDIUM" : "LOW");
        result.setSummary(StringUtils.hasText(item.getSummary()) ? item.getSummary() : item.getTitle());
        result.setReason("本地规则根据关键词命中、标题和摘要相关性评估。");
        result.setTags(List.of("AI", keyword));
        result.setEventType(text.contains("funding") ? "融资" : text.contains("release") || text.contains("launch") ? "产品发布" : "行业趋势");
        return result;
    }

    @SneakyThrows
    private String sha256(String input) {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }

    @SneakyThrows
    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    @SneakyThrows
    private <T> T read(String json, Class<T> type) {
        return objectMapper.readValue(json, type);
    }

    @SneakyThrows
    private List<String> readList(String json) {
        return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
    }

    // 小工具：读取缓存（优先 Redis，其次内存并检查过期）
    private String getValue(String key) {
        if (redisTemplate != null) {
            return redisTemplate.opsForValue().get(key);
        }
        Long exp = localExpiry.get(key);
        if (exp != null && Instant.now().toEpochMilli() > exp) {
            localCache.remove(key);
            localExpiry.remove(key);
            return null;
        }
        return localCache.get(key);
    }

    // 小工具：设置缓存（优先 Redis，其次内存并记录过期时间）
    private void setValue(String key, String value, Duration ttl) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, value, ttl);
            return;
        }
        localCache.put(key, value);
        localExpiry.put(key, Instant.now().plus(ttl).toEpochMilli());
    }
}
