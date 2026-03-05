package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.request.InterviewRequest;
import com.demo.talentbridge.dto.response.ApiResponse;
import com.demo.talentbridge.dto.response.InterviewResponse;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.enums.InterviewStatus;
import com.demo.talentbridge.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    /** Employer: schedule a new interview */
    @PostMapping
    public ResponseEntity<ApiResponse<InterviewResponse>> schedule(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody InterviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Interview scheduled",
                interviewService.scheduleInterview(user.getId(), request)));
    }

    /** Employer: update interview details (reschedule) */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InterviewResponse>> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody InterviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Interview updated",
                interviewService.updateInterview(user.getId(), id, request)));
    }

    /** Employer: update interview status only */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<InterviewResponse>> updateStatus(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestParam InterviewStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                interviewService.updateInterviewStatus(user.getId(), id, status)));
    }

    /** Employer: cancel interview */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        interviewService.cancelInterview(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Interview cancelled", null));
    }

    /** Get interviews for a specific application */
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getByApplication(
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.success(
                interviewService.getInterviewsByApplication(applicationId)));
    }

    /** Candidate: get all my interviews */
    @GetMapping("/my-interviews")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getMyInterviews(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(
                interviewService.getInterviewsForCandidate(user.getId())));
    }

    /** Employer: get all interviews for my jobs */
    @GetMapping("/employer")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getEmployerInterviews(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(
                interviewService.getInterviewsForEmployer(user.getId())));
    }
}
