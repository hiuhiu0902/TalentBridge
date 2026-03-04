package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.request.ApplicationRequest;
import com.demo.talentbridge.dto.request.UpdateApplicationStatusRequest;
import com.demo.talentbridge.dto.response.ApplicationResponse;

import java.util.List;

public interface ApplicationService {
    ApplicationResponse applyForJob(Long candidateUserId, ApplicationRequest request);
    ApplicationResponse updateStatus(Long employerUserId, Long applicationId, UpdateApplicationStatusRequest request);
    ApplicationResponse getApplicationById(Long applicationId);
    List<ApplicationResponse> getApplicationsByCandidate(Long candidateUserId);
    List<ApplicationResponse> getApplicationsByJobPost(Long employerUserId, Long jobPostId);
    List<ApplicationResponse> getApplicationsByEmployer(Long employerUserId);
    void withdrawApplication(Long candidateUserId, Long applicationId);
}
