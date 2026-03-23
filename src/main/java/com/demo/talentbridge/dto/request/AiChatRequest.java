package com.demo.talentbridge.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AiChatRequest {

    @NotBlank(message = "Message cannot be blank")
    @Size(max = 4000, message = "Message must not exceed 4000 characters")
    private String message;

    @Valid
    @Size(max = 20, message = "History must not exceed 20 messages")
    private List<AiChatMessageRequest> history;
}