package com.demo.talentbridge.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationRequest {

    @NotNull(message = "Job post ID is required")
    private Long jobPostId;

    private String cvUrlAtTime;

    private String coverLetter;
}
