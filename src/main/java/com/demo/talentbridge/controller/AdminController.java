package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.request.AdminJobActionRequest;
import com.demo.talentbridge.dto.response.ApiResponse;
import com.demo.talentbridge.dto.response.JobPostResponse;
import com.demo.talentbridge.dto.response.UserResponse;
import com.demo.talentbridge.entity.JobPost;
import com.demo.talentbridge.enums.JobStatus;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.repository.JobPostRepository;
import com.demo.talentbridge.service.JobPostService;
import com.demo.talentbridge.service.NotificationService;
import com.demo.talentbridge.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private JobPostService jobPostService;
    @Autowired private JobPostRepository jobPostRepository;
    @Autowired private UserService userService;
    @Autowired private NotificationService notificationService;

    @GetMapping("/jobs/pending")
    public ResponseEntity<ApiResponse<List<JobPostResponse>>> getPendingJobs() {
        return ResponseEntity.ok(ApiResponse.success(jobPostService.getPendingJobs()));
    }

    @PutMapping("/jobs/{id}/approve")
    public ResponseEntity<ApiResponse<JobPostResponse>> approveJob(@PathVariable Long id) {
        JobPost job = jobPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobPost", "id", id));
        job.setStatus(JobStatus.ACTIVE);
        jobPostRepository.save(job);
        // Notify followers of new job
        notificationService.notifyFollowersOfNewJob(
                job.getEmployer().getUser().getId(), job.getId(), job.getTitle());
        return ResponseEntity.ok(ApiResponse.success("Job approved", jobPostService.getJobById(id)));
    }

    @PutMapping("/jobs/{id}/reject")
    public ResponseEntity<ApiResponse<JobPostResponse>> rejectJob(
            @PathVariable Long id, @RequestBody AdminJobActionRequest request) {
        JobPost job = jobPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("JobPost", "id", id));
        job.setStatus(JobStatus.REJECTED);
        job.setRejectionReason(request.getRejectionReason());
        jobPostRepository.save(job);
        return ResponseEntity.ok(ApiResponse.success("Job rejected", jobPostService.getJobById(id)));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @PutMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUserStatus(id, true)));
    }

    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUserStatus(id, false)));
    }
}
