package com.demo.talentbridge.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewRequest {

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    @NotNull(message = "Interview time is required")
    private LocalDateTime interviewAt;

    private String location;
    private String meetingLink;
    private String note;
}
