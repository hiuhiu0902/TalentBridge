package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.request.ApplicationRequest;
import com.demo.talentbridge.dto.request.UpdateApplicationStatusRequest;
import com.demo.talentbridge.dto.response.ApiResponse;
import com.demo.talentbridge.dto.response.ApplicationResponse;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {
    @Autowired private ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponse>> apply(
            @AuthenticationPrincipal User user, @Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Applied successfully", applicationService.applyForJob(user.getId(), request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplication(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getApplicationById(id)));
    }

    @GetMapping("/my-applications")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getMyApplications(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getApplicationsByCandidate(user.getId())));
    }

    @GetMapping("/job/{jobPostId}")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getByJobPost(
            @AuthenticationPrincipal User user, @PathVariable Long jobPostId) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getApplicationsByJobPost(user.getId(), jobPostId)));
    }

    @GetMapping("/employer")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getByEmployer(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getApplicationsByEmployer(user.getId())));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateStatus(
            @AuthenticationPrincipal User user, @PathVariable Long id,
            @Valid @RequestBody UpdateApplicationStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.updateStatus(user.getId(), id, request)));
    }

    @PutMapping("/{id}/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(@AuthenticationPrincipal User user, @PathVariable Long id) {
        applicationService.withdrawApplication(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Application withdrawn", null));
    }
}
