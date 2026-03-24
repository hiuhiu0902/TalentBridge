package com.demo.talentbridge.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiChatSessionCreateRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
}
