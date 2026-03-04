package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.response.JobPostResponse;
import com.demo.talentbridge.entity.*;
import com.demo.talentbridge.exception.DuplicateResourceException;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.repository.*;
import com.demo.talentbridge.service.JobPostService;
import com.demo.talentbridge.service.SavedJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavedJobServiceImpl implements SavedJobService {
    @Autowired private SavedJobRepository savedJobRepository;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private JobPostRepository jobPostRepository;
    @Autowired private JobPostService jobPostService;

    @Override @Transactional
    public void saveJob(Long candidateUserId, Long jobPostId) {
        Candidate c = candidateRepository.findByUserId(candidateUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
        if (savedJobRepository.existsByCandidateIdAndJobPostId(c.getId(), jobPostId))
            throw new DuplicateResourceException("Job already saved");
        JobPost jp = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new ResourceNotFoundException("JobPost","id",jobPostId));
        savedJobRepository.save(SavedJob.builder().candidate(c).jobPost(jp).build());
    }

    @Override @Transactional
    public void unsaveJob(Long candidateUserId, Long jobPostId) {
        Candidate c = candidateRepository.findByUserId(candidateUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
        savedJobRepository.deleteByCandidateIdAndJobPostId(c.getId(), jobPostId);
    }

    @Override
    public List<JobPostResponse> getSavedJobs(Long candidateUserId) {
        Candidate c = candidateRepository.findByUserId(candidateUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
        return savedJobRepository.findByCandidateId(c.getId()).stream()
                .map(sj -> jobPostService.getJobById(sj.getJobPost().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isJobSaved(Long candidateUserId, Long jobPostId) {
        Candidate c = candidateRepository.findByUserId(candidateUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found"));
        return savedJobRepository.existsByCandidateIdAndJobPostId(c.getId(), jobPostId);
    }
}
