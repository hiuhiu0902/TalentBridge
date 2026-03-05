package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.request.AdminJobActionRequest;
import com.demo.talentbridge.dto.request.CategoryRequest;
import com.demo.talentbridge.dto.response.*;
import com.demo.talentbridge.enums.JobStatus;
import com.demo.talentbridge.enums.UserRole;
import com.demo.talentbridge.repository.ApplicationRepository;
import com.demo.talentbridge.repository.CandidateRepository;
import com.demo.talentbridge.repository.CategoryRepository;
import com.demo.talentbridge.repository.EmployerRepository;
import com.demo.talentbridge.repository.JobPostRepository;
import com.demo.talentbridge.repository.UserRepository;
import com.demo.talentbridge.service.CategoryService;
import com.demo.talentbridge.service.JobPostService;
import com.demo.talentbridge.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    @Autowired private JobPostService jobPostService;
    @Autowired private UserService userService;
    @Autowired private CategoryService categoryService;
    @Autowired private UserRepository userRepository;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private EmployerRepository employerRepository;
    @Autowired private JobPostRepository jobPostRepository;
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private CategoryRepository categoryRepository;

    // ─── Job Management ─────────────────────────────────────────────────────────

    @GetMapping("/jobs/pending")
    public ResponseEntity<ApiResponse<List<JobPostResponse>>> getPendingJobs() {
        return ResponseEntity.ok(ApiResponse.success(jobPostService.getPendingJobs()));
    }

    @PutMapping("/jobs/{id}/approve")
    public ResponseEntity<ApiResponse<JobPostResponse>> approveJob(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Job approved", jobPostService.approveJob(id)));
    }

    @PutMapping("/jobs/{id}/reject")
    public ResponseEntity<ApiResponse<JobPostResponse>> rejectJob(
            @PathVariable Long id,
            @RequestBody AdminJobActionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Job rejected",
                jobPostService.rejectJob(id, request.getRejectionReason())));
    }

    // ─── User Management ────────────────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @PutMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User activated", userService.updateUserStatus(id, true)));
    }

    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User deactivated", userService.updateUserStatus(id, false)));
    }

    // ─── Category Management (Admin-only CRUD) ──────────────────────────────────

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Category created", categoryService.createCategory(request)));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Category updated", categoryService.updateCategory(id, request)));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }
}
