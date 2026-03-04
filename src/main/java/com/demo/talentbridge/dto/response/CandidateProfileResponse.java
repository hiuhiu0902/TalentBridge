package com.demo.talentbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateProfileResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String address;
    private String summary;
    private String cvUrl;
    private String avatarUrl;
    private String email;
    private List<EducationResponse> educations;
    private List<WorkExperienceResponse> workExperiences;
    private List<SkillResponse> skills;
}
