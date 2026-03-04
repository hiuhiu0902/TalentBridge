package com.demo.talentbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EducationRequest {

    @NotBlank(message = "School name is required")
    private String school;

    private String major;
    private String degree;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
