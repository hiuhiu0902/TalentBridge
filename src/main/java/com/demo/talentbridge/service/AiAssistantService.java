package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.request.AiChatRequest;
import com.demo.talentbridge.dto.request.AiChatSessionCreateRequest;
import com.demo.talentbridge.dto.request.AiSessionMessageRequest;
import com.demo.talentbridge.dto.response.AiChatMessageResponse;
import com.demo.talentbridge.dto.response.AiChatResponse;
import com.demo.talentbridge.dto.response.AiChatSessionResponse;

import java.util.List;

public interface AiAssistantService {
    AiChatResponse chat(Long userId, AiChatRequest request);
    AiChatSessionResponse createSession(Long userId, AiChatSessionCreateRequest request);
    List<AiChatSessionResponse> getMySessions(Long userId);
    List<AiChatMessageResponse> getSessionMessages(Long userId, Long sessionId);
    AiChatResponse chatInSession(Long userId, Long sessionId, AiSessionMessageRequest request);
}
