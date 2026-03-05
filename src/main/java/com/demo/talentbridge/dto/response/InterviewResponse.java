package com.demo.talentbridge.dto.response;

import com.demo.talentbridge.enums.InterviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResponse {
    private Long id;
    private Long applicationId;
    private String jobTitle;
    private String candidateName;
    private LocalDateTime interviewAt;
    private String location;
    private String meetingLink;
    private String note;
    private InterviewStatus status;
    private LocalDateTime createdAt;
}
