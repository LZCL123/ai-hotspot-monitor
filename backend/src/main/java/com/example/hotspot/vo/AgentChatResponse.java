package com.example.hotspot.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AI 智能客服响应体。
 * 包含 AI 生成的回答内容和相关的热点引用列表。
 */
@Data
@AllArgsConstructor
public class AgentChatResponse {
    private String answer;
    private List<AgentReference> references;
}
