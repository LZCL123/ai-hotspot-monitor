package com.example.hotspot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 智能客服请求体。
 */
@Data
public class AgentChatRequest {
    @NotBlank
    private String question;
}
