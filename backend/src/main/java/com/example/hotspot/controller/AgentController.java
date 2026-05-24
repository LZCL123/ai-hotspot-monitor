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

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {
    private final AgentService agentService;

    @PostMapping("/chat")
    public ApiResponse<AgentChatResponse> chat(@Valid @RequestBody AgentChatRequest request) {
        return ApiResponse.ok(agentService.chat(request.getQuestion()));
    }
}
