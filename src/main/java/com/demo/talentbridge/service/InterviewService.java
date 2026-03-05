package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.request.InterviewRequest;
import com.demo.talentbridge.dto.response.InterviewResponse;
import com.demo.talentbridge.enums.InterviewStatus;

import java.util.List;

public interface InterviewService {
    InterviewResponse scheduleInterview(Long employerUserId, InterviewRequest request);
    InterviewResponse updateInterview(Long employerUserId, Long interviewId, InterviewRequest request);
    InterviewResponse updateInterviewStatus(Long employerUserId, Long interviewId, InterviewStatus status);
    void cancelInterview(Long employerUserId, Long interviewId);
    List<InterviewResponse> getInterviewsByApplication(Long applicationId);
    List<InterviewResponse> getInterviewsForCandidate(Long candidateUserId);
    List<InterviewResponse> getInterviewsForEmployer(Long employerUserId);
}
