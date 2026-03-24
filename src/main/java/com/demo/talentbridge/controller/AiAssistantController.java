package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.request.AiChatRequest;
import com.demo.talentbridge.dto.request.AiChatSessionCreateRequest;
import com.demo.talentbridge.dto.request.AiSessionMessageRequest;
import com.demo.talentbridge.dto.response.AiChatMessageResponse;
import com.demo.talentbridge.dto.response.AiChatResponse;
import com.demo.talentbridge.dto.response.AiChatSessionResponse;
import com.demo.talentbridge.dto.response.ApiResponse;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.service.AiAssistantService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@SecurityRequirement(name = "bearerAuth")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(
            @org.springframework.security.core.annotation.AuthenticationPrincipal User user,
            @Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "AI response generated",
                aiAssistantService.chat(user.getId(), request)));
    }

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<AiChatSessionResponse>> createSession(
            @org.springframework.security.core.annotation.AuthenticationPrincipal User user,
            @Valid @RequestBody(required = false) AiChatSessionCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "AI chat session created",
                aiAssistantService.createSession(user.getId(), request)));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<AiChatSessionResponse>>> getSessions(
            @org.springframework.security.core.annotation.AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(aiAssistantService.getMySessions(user.getId())));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<List<AiChatMessageResponse>>> getMessages(
            @org.springframework.security.core.annotation.AuthenticationPrincipal User user,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success(aiAssistantService.getSessionMessages(user.getId(), sessionId)));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<AiChatResponse>> sendMessage(
            @org.springframework.security.core.annotation.AuthenticationPrincipal User user,
            @PathVariable Long sessionId,
            @Valid @RequestBody AiSessionMessageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "AI response generated",
                aiAssistantService.chatInSession(user.getId(), sessionId, request)));
    }
}
