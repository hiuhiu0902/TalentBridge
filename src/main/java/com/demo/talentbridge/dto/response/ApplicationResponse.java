package com.demo.talentbridge.dto.response;

import com.demo.talentbridge.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;
    private ApplicationStatus status;
    private String cvUrlAtTime;
    private String coverLetter;
    private LocalDateTime appliedAt;
    private Long jobPostId;
    private String jobTitle;
    private String companyName;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private List<ApplicationHistoryResponse> histories;
}
