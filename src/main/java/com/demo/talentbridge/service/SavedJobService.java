package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.response.JobPostResponse;

import java.util.List;

public interface SavedJobService {
    void saveJob(Long candidateUserId, Long jobPostId);
    void unsaveJob(Long candidateUserId, Long jobPostId);
    List<JobPostResponse> getSavedJobs(Long candidateUserId);
    boolean isJobSaved(Long candidateUserId, Long jobPostId);
}
