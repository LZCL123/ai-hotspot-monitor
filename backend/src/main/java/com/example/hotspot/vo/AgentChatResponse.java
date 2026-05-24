package com.example.hotspot.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AgentChatResponse {
    private String answer;
    private List<AgentReference> references;
}
