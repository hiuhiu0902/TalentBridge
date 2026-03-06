package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.request.*;
import com.demo.talentbridge.dto.response.*;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.service.CandidateService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/candidates")
@SecurityRequirement(name = "bearerAuth")

public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    // ─── Profile ────────────────────────────────────────────────────────────────

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> getMyProfile(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(candidateService.getProfile(user.getId())));
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> getProfile(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(candidateService.getProfile(userId)));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CandidateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(candidateService.updateProfile(user.getId(), request)));
    }

    @PostMapping("/profile")
    public ResponseEntity<CandidateProfileResponse> createProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CandidateProfileRequest request) {

        return ResponseEntity.ok(candidateService.createProfile(user.getId(), request));
    }
    // ─── Education ──────────────────────────────────────────────────────────────

    @GetMapping("/education")
    public ResponseEntity<ApiResponse<List<EducationResponse>>> getEducations(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(candidateService.getEducations(user.getId())));
    }

    @PostMapping("/education")
    public ResponseEntity<ApiResponse<EducationResponse>> addEducation(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody EducationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Education added", candidateService.addEducation(user.getId(), request)));
    }

    @PutMapping("/education/{id}")
    public ResponseEntity<ApiResponse<EducationResponse>> updateEducation(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody EducationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(candidateService.updateEducation(user.getId(), id, request)));
    }

    @DeleteMapping("/education/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEducation(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        candidateService.deleteEducation(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Education deleted", null));
    }

    // ─── Work Experience ────────────────────────────────────────────────────────

    @GetMapping("/experience")
    public ResponseEntity<ApiResponse<List<WorkExperienceResponse>>> getExperiences(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(candidateService.getWorkExperiences(user.getId())));
    }

    @PostMapping("/experience")
    public ResponseEntity<ApiResponse<WorkExperienceResponse>> addExperience(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody WorkExperienceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Experience added", candidateService.addWorkExperience(user.getId(), request)));
    }

    @PutMapping("/experience/{id}")
    public ResponseEntity<ApiResponse<WorkExperienceResponse>> updateExperience(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody WorkExperienceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(candidateService.updateWorkExperience(user.getId(), id, request)));
    }

    @DeleteMapping("/experience/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExperience(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        candidateService.deleteWorkExperience(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Experience deleted", null));
    }

    // ─── Skills ─────────────────────────────────────────────────────────────────

    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getSkills(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(candidateService.getSkills(user.getId())));
    }

    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<SkillResponse>> addSkill(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CandidateSkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Skill added", candidateService.addSkill(user.getId(), request)));
    }

    @PutMapping("/skills")
    public ResponseEntity<ApiResponse<SkillResponse>> updateSkill(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CandidateSkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Skill updated", candidateService.updateSkill(user.getId(), request)));
    }

    @DeleteMapping("/skills/{skillName}")
    public ResponseEntity<ApiResponse<Void>> removeSkill(
            @AuthenticationPrincipal User user,
            @PathVariable String skillName) {
        candidateService.removeSkill(user.getId(), skillName);
        return ResponseEntity.ok(ApiResponse.success("Skill removed", null));
    }
}
