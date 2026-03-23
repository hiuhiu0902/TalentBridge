package com.demo.talentbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatMessageRequest {

    @NotBlank(message = "Role is required")
    @Size(max = 20, message = "Role is too long")
    private String role;

    @NotBlank(message = "Content is required")
    @Size(max = 4000, message = "Content must not exceed 4000 characters")
    private String content;
}