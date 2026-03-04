package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.request.InterviewRequest;
import com.demo.talentbridge.dto.response.InterviewResponse;
import com.demo.talentbridge.enums.InterviewStatus;

import java.util.List;

public interface InterviewService {
    InterviewResponse scheduleInterview(Long employerUserId, InterviewRequest request);
    InterviewResponse updateInterviewStatus(Long employerUserId, Long interviewId, InterviewStatus status);
    List<InterviewResponse> getInterviewsByApplication(Long applicationId);
}
