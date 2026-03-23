package com.demo.talentbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    private String answer;
    private String model;
    private boolean denied;
    private List<String> usedTools;
    private Integer toolCallCount;
    private LocalDateTime generatedAt;
}
