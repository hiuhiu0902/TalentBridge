package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.response.ApiResponse;
import com.demo.talentbridge.dto.response.JobPostResponse;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.service.SavedJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/saved-jobs")
public class SavedJobController {
    @Autowired private SavedJobService savedJobService;

    @PostMapping("/{jobPostId}")
    public ResponseEntity<ApiResponse<Void>> saveJob(@AuthenticationPrincipal User user, @PathVariable Long jobPostId) {
        savedJobService.saveJob(user.getId(), jobPostId);
        return ResponseEntity.ok(ApiResponse.success("Job saved", null));
    }

    @DeleteMapping("/{jobPostId}")
    public ResponseEntity<ApiResponse<Void>> unsaveJob(@AuthenticationPrincipal User user, @PathVariable Long jobPostId) {
        savedJobService.unsaveJob(user.getId(), jobPostId);
        return ResponseEntity.ok(ApiResponse.success("Job unsaved", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobPostResponse>>> getSavedJobs(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(savedJobService.getSavedJobs(user.getId())));
    }

    @GetMapping("/{jobPostId}/check")
    public ResponseEntity<ApiResponse<Boolean>> isJobSaved(@AuthenticationPrincipal User user, @PathVariable Long jobPostId) {
        return ResponseEntity.ok(ApiResponse.success(savedJobService.isJobSaved(user.getId(), jobPostId)));
    }
}
