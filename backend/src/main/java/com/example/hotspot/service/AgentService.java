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

/**
 * AI 智能客服服务。
 * 根据用户提问从热点库中检索最相关的热点作为参考资料，调用 AI 生成回答。
 */
@Service
@RequiredArgsConstructor
public class AgentService {
    private static final int CANDIDATE_LIMIT = 200;
    private static final int REFERENCE_LIMIT = 5;

    private final HotspotMapper hotspotMapper;
    private final AiService aiService;

    /**
     * 智能问答
     *
     * @param question 用户问题
     * @return 包含 AI 回答和相关热点的响应
     */
    public AgentChatResponse chat(String question) {
        List<AgentReference> references = retrieve(question);
        String answer = aiService.answerWithReferences(question, references);
        return new AgentChatResponse(answer, references);
    }

    /**
     * 检索相关热点
     * 对问题进行分词后，从最近 200 条热点中评分排序，返回最相关的 5 条参考。
     *
     * @param question 用户问题
     * @return 相关热点引用列表
     */
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

    /**
     * 计算热点与问题的相关性评分
     * 根据关键词命中标题、关键词、摘要、标签等字段的情况累加得分，并参考已有的相关性分数。
     *
     * @param hotspot 热点对象
     * @param tokens  分词后的关键词列表
     * @return 相关性评分
     */
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

    /**
     * 对文本进行分词处理
     *
     * @param question 原始文本
     * @return 去重后的分词列表
     */
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
