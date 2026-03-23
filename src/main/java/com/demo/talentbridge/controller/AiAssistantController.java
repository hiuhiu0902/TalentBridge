package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.request.AiChatRequest;
import com.demo.talentbridge.dto.response.AiChatResponse;
import com.demo.talentbridge.dto.response.ApiResponse;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.service.AiAssistantService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
@SecurityRequirement(name = "bearerAuth")
public class AiAssistantController {

    @Autowired
    private AiAssistantService aiAssistantService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AiChatRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "AI response generated",
                aiAssistantService.chat(user.getId(), request)));
    }
}
