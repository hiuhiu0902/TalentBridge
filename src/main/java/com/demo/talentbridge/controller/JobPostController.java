package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.request.JobPostRequest;
import com.demo.talentbridge.dto.response.ApiResponse;
import com.demo.talentbridge.dto.response.JobPostResponse;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.enums.SkillName;
import com.demo.talentbridge.service.JobPostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@SecurityRequirement(name = "bearerAuth")

public class JobPostController {

    @Autowired
    private JobPostService jobPostService;

    @PostMapping
    public ResponseEntity<ApiResponse<JobPostResponse>> createJob(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody JobPostRequest request) {
        JobPostResponse job = jobPostService.createJob(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Job created, pending approval", job));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobPostResponse>> updateJob(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody JobPostRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Job updated, pending re-approval", jobPostService.updateJob(user.getId(), id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> closeJob(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        jobPostService.closeJob(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Job closed", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobPostResponse>> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(jobPostService.getJobById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobPostResponse>>> getActiveJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "postedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ApiResponse.success(jobPostService.getAllActiveJobs(pageable)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<JobPostResponse>>> searchJobs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                jobPostService.searchJobs(keyword, PageRequest.of(page, size))));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<JobPostResponse>>> getByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                jobPostService.getJobsByCategory(categoryId, PageRequest.of(page, size))));
    }

    @GetMapping("/by-skills")
    public ResponseEntity<ApiResponse<Page<JobPostResponse>>> getBySkills(
            @RequestParam List<SkillName> skills,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                jobPostService.getJobsBySkills(skills, PageRequest.of(page, size))));
    }

    @GetMapping("/my-jobs")
    public ResponseEntity<ApiResponse<List<JobPostResponse>>> getMyJobs(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(jobPostService.getJobsByEmployer(user.getId())));
    }

    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<List<JobPostResponse>>> getFeed(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                jobPostService.getJobFeed(user.getId(), PageRequest.of(page, size))));
    }
}
