package com.demo.talentbridge.service.support;

import com.demo.talentbridge.dto.ai.CandidateMatchResult;
import com.demo.talentbridge.entity.Application;
import com.demo.talentbridge.entity.Employer;
import com.demo.talentbridge.entity.JobPost;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.exception.UnauthorizedException;
import com.demo.talentbridge.repository.ApplicationRepository;
import com.demo.talentbridge.repository.EmployerRepository;
import com.demo.talentbridge.repository.JobPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CandidateRankingService {

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private JobPostRepository jobPostRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobMatchingService jobMatchingService;

    public Map<String, Object> recommendCandidatesForJob(Long employerUserId, Long jobPostId, Integer limit) {
        Employer employer = employerRepository.findByUserId(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found for user: " + employerUserId));

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new ResourceNotFoundException("JobPost", "id", jobPostId));

        if (jobPost.getEmployer() == null || !jobPost.getEmployer().getId().equals(employer.getId())) {
            throw new UnauthorizedException("You don't have permission to rank candidates for this job");
        }

        int safeLimit = clampLimit(limit, 5);

        List<CandidateMatchResult> rankedCandidates = applicationRepository.findByJobPostId(jobPostId).stream()
                .filter(Objects::nonNull)
                .map(application -> jobMatchingService.scoreCandidateForJob(application.getCandidate(), jobPost, application))
                .sorted(Comparator.comparing(CandidateMatchResult::matchScore).reversed())
                .limit(safeLimit)
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("jobPostId", jobPost.getId());
        result.put("jobTitle", jobPost.getTitle());
        result.put("companyName", jobPost.getEmployer() != null ? jobPost.getEmployer().getCompanyName() : null);
        result.put("count", rankedCandidates.size());
        result.put("candidates", rankedCandidates);
        return result;
    }

    private int clampLimit(Integer limit, int defaultValue) {
        if (limit == null) {
            return defaultValue;
        }
        return Math.max(1, Math.min(limit, 20));
    }
}