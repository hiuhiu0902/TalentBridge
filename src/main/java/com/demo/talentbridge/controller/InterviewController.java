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
    @Autowired private InterviewService interviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<InterviewResponse>> schedule(
            @AuthenticationPrincipal User user, @Valid @RequestBody InterviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Interview scheduled", interviewService.scheduleInterview(user.getId(), request)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<InterviewResponse>> updateStatus(
            @AuthenticationPrincipal User user, @PathVariable Long id, @RequestParam InterviewStatus status) {
        return ResponseEntity.ok(ApiResponse.success(interviewService.updateInterviewStatus(user.getId(), id, status)));
    }

    @GetMapping("/application/{applicationId}")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getByApplication(@PathVariable Long applicationId) {
        return ResponseEntity.ok(ApiResponse.success(interviewService.getInterviewsByApplication(applicationId)));
    }
}
