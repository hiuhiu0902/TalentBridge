package com.demo.talentbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotBlank(message = "Message content is required")
    @Size(max = 4000, message = "Message content must not exceed 4000 characters")
    private String content;
}