package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.request.EmployerProfileRequest;
import com.demo.talentbridge.dto.response.ApiResponse;
import com.demo.talentbridge.dto.response.EmployerProfileResponse;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.service.EmployerService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employers")
@SecurityRequirement(name = "bearerAuth")

public class EmployerController {
    @Autowired private EmployerService employerService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<EmployerProfileResponse>> getMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(employerService.getProfile(user.getId())));
    }

    @GetMapping("/{employerId}/profile")
    public ResponseEntity<ApiResponse<EmployerProfileResponse>> getProfile(@PathVariable Long employerId) {
        return ResponseEntity.ok(ApiResponse.success(employerService.getProfileByEmployerId(employerId)));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<EmployerProfileResponse>> updateProfile(
            @AuthenticationPrincipal User user, @Valid @RequestBody EmployerProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employerService.updateProfile(user.getId(), request)));
    }
    @PostMapping("/profile")
    public ResponseEntity<ApiResponse<EmployerProfileResponse>> createProfile(
            @AuthenticationPrincipal User user, @Valid @RequestBody EmployerProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(employerService.createProfile(user.getId(), request)));
    }
}
