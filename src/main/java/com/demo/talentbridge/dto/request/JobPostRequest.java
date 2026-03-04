package com.demo.talentbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobPostRequest {

    @NotBlank(message = "Job title is required")
    @Size(max = 200)
    private String title;

    private String description;

    private BigDecimal salaryMin;
    private BigDecimal salaryMax;

    @Size(max = 100)
    private String location;

    @Size(max = 50)
    private String jobType;

    @Size(max = 50)
    private String experienceLevel;

    private Long categoryId;

    private LocalDateTime expiredAt;

    private List<JobSkillRequest> skills;
}
