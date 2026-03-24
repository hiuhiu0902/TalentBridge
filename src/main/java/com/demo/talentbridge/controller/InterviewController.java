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
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/interviews")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<InterviewResponse>> schedule(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody InterviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Interview scheduled",
                interviewService.scheduleInterview(user.getId(), request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InterviewResponse>> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody InterviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Interview updated",
                interviewService.updateInterview(user.getId(), id, request)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<InterviewResponse>> updateStatus(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestParam InterviewStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                interviewService.updateInterviewStatus(user.getId(), id, status)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        interviewService.cancelInterview(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Interview cancelled", null));
    }

    // detail interview for notification "xem chi tiết"
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InterviewResponse>> getById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                interviewService.getInterviewById(user.getId(), id)));
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getByApplication(
            @AuthenticationPrincipal User user,
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.success(
                interviewService.getInterviewsByApplication(user.getId(), applicationId)));
    }

    @GetMapping("/my-interviews")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getMyInterviews(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(
                interviewService.getInterviewsForCandidate(user.getId())));
    }

    @GetMapping("/employer")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getEmployerInterviews(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(
                interviewService.getInterviewsForEmployer(user.getId())));
    }
}