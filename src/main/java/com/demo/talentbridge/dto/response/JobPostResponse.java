package com.demo.talentbridge.dto.response;

import com.demo.talentbridge.enums.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String location;
    private String jobType;
    private String experienceLevel;
    private JobStatus status;
    private LocalDateTime postedAt;
    private LocalDateTime expiredAt;
    private String rejectionReason;
    private Long employerId;
    private String companyName;
    private String logoUrl;
    private Long categoryId;
    private String categoryName;
    private List<SkillResponse> skills;
    private Integer applicationCount;
    private boolean savedByCurrentUser;
}
