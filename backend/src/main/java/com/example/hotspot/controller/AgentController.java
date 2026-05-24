package com.example.hotspot.controller;

import com.example.hotspot.common.ApiResponse;
import com.example.hotspot.dto.AgentChatRequest;
import com.example.hotspot.service.AgentService;
import com.example.hotspot.vo.AgentChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 智能客服控制器。
 * 提供基于已采集热点数据的智能问答接口，支持流式对话和参考来源返回。
 */
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {
    private final AgentService agentService;

    /**
     * 智能问答
     *
     * @param request 用户提问请求体
     * @return 包含回答内容和参考来源的响应
     */
    @PostMapping("/chat")
    public ApiResponse<AgentChatResponse> chat(@Valid @RequestBody AgentChatRequest request) {
        return ApiResponse.ok(agentService.chat(request.getQuestion()));
    }
}
