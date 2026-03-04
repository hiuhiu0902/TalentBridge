package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.request.JobPostRequest;
import com.demo.talentbridge.dto.response.JobPostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobPostService {
    JobPostResponse createJob(Long employerUserId, JobPostRequest request);
    JobPostResponse updateJob(Long employerUserId, Long jobId, JobPostRequest request);
    void closeJob(Long employerUserId, Long jobId);
    JobPostResponse getJobById(Long jobId);
    Page<JobPostResponse> getAllActiveJobs(Pageable pageable);
    Page<JobPostResponse> searchJobs(String keyword, Pageable pageable);
    Page<JobPostResponse> getJobsByCategory(Long categoryId, Pageable pageable);
    List<JobPostResponse> getJobsByEmployer(Long employerUserId);
    List<JobPostResponse> getJobFeed(Long candidateUserId, Pageable pageable);
    List<JobPostResponse> getPendingJobs();
}
