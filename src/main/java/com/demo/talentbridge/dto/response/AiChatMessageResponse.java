package com.demo.talentbridge.dto.response;

import com.demo.talentbridge.enums.AiChatActorType;
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
public class AiChatMessageResponse {
    private Long id;
    private Long sessionId;
    private AiChatActorType senderType;
    private String content;
    private String modelName;
    private List<String> usedTools;
    private LocalDateTime createdAt;
}
