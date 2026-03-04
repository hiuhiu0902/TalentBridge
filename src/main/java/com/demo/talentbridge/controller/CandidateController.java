package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.request.*;
import com.demo.talentbridge.dto.response.*;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.service.CandidateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/candidates")
public class CandidateController {
    @Autowired private CandidateService candidateService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> getMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(candidateService.getProfile(user.getId())));
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(candidateService.getProfile(userId)));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> updateProfile(
            @AuthenticationPrincipal User user, @Valid @RequestBody CandidateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(candidateService.updateProfile(user.getId(), request)));
    }

    @PostMapping("/education")
    public ResponseEntity<ApiResponse<EducationResponse>> addEducation(
            @AuthenticationPrincipal User user, @Valid @RequestBody EducationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(candidateService.addEducation(user.getId(), request)));
    }

    @PutMapping("/education/{id}")
    public ResponseEntity<ApiResponse<EducationResponse>> updateEducation(
            @AuthenticationPrincipal User user, @PathVariable Long id, @Valid @RequestBody EducationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(candidateService.updateEducation(user.getId(), id, request)));
    }

    @DeleteMapping("/education/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEducation(@AuthenticationPrincipal User user, @PathVariable Long id) {
        candidateService.deleteEducation(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Deleted", null));
    }

    @PostMapping("/experience")
    public ResponseEntity<ApiResponse<WorkExperienceResponse>> addExperience(
            @AuthenticationPrincipal User user, @Valid @RequestBody WorkExperienceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(candidateService.addWorkExperience(user.getId(), request)));
    }

    @PutMapping("/experience/{id}")
    public ResponseEntity<ApiResponse<WorkExperienceResponse>> updateExperience(
            @AuthenticationPrincipal User user, @PathVariable Long id, @Valid @RequestBody WorkExperienceRequest request) {
        return ResponseEntity.ok(ApiResponse.success(candidateService.updateWorkExperience(user.getId(), id, request)));
    }

    @DeleteMapping("/experience/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExperience(@AuthenticationPrincipal User user, @PathVariable Long id) {
        candidateService.deleteWorkExperience(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Deleted", null));
    }

    @PostMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<Void>> addSkill(
            @AuthenticationPrincipal User user, @PathVariable Long skillId,
            @RequestParam(required = false) String level) {
        candidateService.addSkill(user.getId(), skillId, level);
        return ResponseEntity.ok(ApiResponse.success("Skill added", null));
    }

    @DeleteMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<Void>> removeSkill(@AuthenticationPrincipal User user, @PathVariable Long skillId) {
        candidateService.removeSkill(user.getId(), skillId);
        return ResponseEntity.ok(ApiResponse.success("Skill removed", null));
    }
}
