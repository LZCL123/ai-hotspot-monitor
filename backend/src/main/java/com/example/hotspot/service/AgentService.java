package com.example.hotspot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.hotspot.entity.Hotspot;
import com.example.hotspot.mapper.HotspotMapper;
import com.example.hotspot.vo.AgentChatResponse;
import com.example.hotspot.vo.AgentReference;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AgentService {
    private static final int CANDIDATE_LIMIT = 200;
    private static final int REFERENCE_LIMIT = 5;

    private final HotspotMapper hotspotMapper;
    private final AiService aiService;

    public AgentChatResponse chat(String question) {
        List<AgentReference> references = retrieve(question);
        String answer = aiService.answerWithReferences(question, references);
        return new AgentChatResponse(answer, references);
    }

    private List<AgentReference> retrieve(String question) {
        List<String> tokens = tokenize(question);
        List<Hotspot> candidates = hotspotMapper.selectList(new LambdaQueryWrapper<Hotspot>()
                .orderByDesc(Hotspot::getCreatedAt)
                .last("LIMIT " + CANDIDATE_LIMIT));

        return candidates.stream()
                .map(item -> new ScoredHotspot(item, score(item, tokens)))
                .filter(item -> item.score() > 0)
                .sorted(Comparator.comparingInt(ScoredHotspot::score).reversed())
                .limit(REFERENCE_LIMIT)
                .map(item -> toReference(item.hotspot()))
                .toList();
    }

    private int score(Hotspot hotspot, List<String> tokens) {
        String text = normalize(String.join(" ",
                nullToEmpty(hotspot.getTitle()),
                nullToEmpty(hotspot.getKeyword()),
                nullToEmpty(hotspot.getSummary()),
                nullToEmpty(hotspot.getRawSummary()),
                nullToEmpty(hotspot.getTags()),
                nullToEmpty(hotspot.getEventType())));
        int score = 0;
        for (String token : tokens) {
            if (token.length() < 2) {
                continue;
            }
            if (text.contains(token)) {
                score += 10;
            }
            if (normalize(nullToEmpty(hotspot.getTitle())).contains(token)) {
                score += 8;
            }
            if (normalize(nullToEmpty(hotspot.getKeyword())).contains(token)) {
                score += 12;
            }
        }
        if (hotspot.getRelevanceScore() != null) {
            score += Math.max(0, hotspot.getRelevanceScore() / 20);
        }
        return score;
    }

    private List<String> tokenize(String question) {
        String normalized = normalize(question);
        return Arrays.stream(normalized.split("\\s+"))
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    private String normalize(String value) {
        return nullToEmpty(value)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", " ")
                .trim();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private AgentReference toReference(Hotspot hotspot) {
        return new AgentReference(
                hotspot.getId(),
                hotspot.getTitle(),
                hotspot.getUrl(),
                hotspot.getSource(),
                hotspot.getKeyword(),
                StringUtils.hasText(hotspot.getSummary()) ? hotspot.getSummary() : hotspot.getRawSummary(),
                hotspot.getRelevanceScore());
    }

    private record ScoredHotspot(Hotspot hotspot, int score) {
    }
}
