package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.request.*;
import com.demo.talentbridge.dto.response.CandidateProfileResponse;
import com.demo.talentbridge.dto.response.EducationResponse;
import com.demo.talentbridge.dto.response.SkillResponse;
import com.demo.talentbridge.dto.response.WorkExperienceResponse;

import java.util.List;

public interface CandidateService {
    CandidateProfileResponse getProfile(Long userId);
    CandidateProfileResponse updateProfile(Long userId, CandidateProfileRequest request);
    CandidateProfileResponse createProfile(Long userId, CandidateProfileRequest request);


    EducationResponse addEducation(Long userId, EducationRequest request);
    EducationResponse updateEducation(Long userId, Long educationId, EducationRequest request);
    void deleteEducation(Long userId, Long educationId);
    List<EducationResponse> getEducations(Long userId);

    WorkExperienceResponse addWorkExperience(Long userId, WorkExperienceRequest request);
    WorkExperienceResponse updateWorkExperience(Long userId, Long expId, WorkExperienceRequest request);
    void deleteWorkExperience(Long userId, Long expId);
    List<WorkExperienceResponse> getWorkExperiences(Long userId);

    SkillResponse addSkill(Long userId, CandidateSkillRequest request);
    SkillResponse updateSkill(Long userId, CandidateSkillRequest request);
    void removeSkill(Long userId, String skillName);
    List<SkillResponse> getSkills(Long userId);
}
