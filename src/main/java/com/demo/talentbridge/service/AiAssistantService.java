package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.request.AiChatRequest;
import com.demo.talentbridge.dto.response.AiChatResponse;

public interface AiAssistantService {
    AiChatResponse chat(Long userId, AiChatRequest request);
}
