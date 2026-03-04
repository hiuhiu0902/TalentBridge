package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.request.JobPostRequest;
import com.demo.talentbridge.dto.request.JobSkillRequest;
import com.demo.talentbridge.dto.response.JobPostResponse;
import com.demo.talentbridge.dto.response.SkillResponse;
import com.demo.talentbridge.entity.*;
import com.demo.talentbridge.enums.JobStatus;
import com.demo.talentbridge.exception.BadRequestException;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.exception.UnauthorizedException;
import com.demo.talentbridge.repository.*;
import com.demo.talentbridge.service.JobPostService;
import com.demo.talentbridge.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobPostServiceImpl implements JobPostService {

    @Autowired
    private JobPostRepository jobPostRepository;

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public JobPostResponse createJob(Long employerUserId, JobPostRequest request) {
        Employer employer = employerRepository.findByUserId(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found for user: " + employerUserId));

        JobPost jobPost = JobPost.builder()
                .employer(employer)
                .title(request.getTitle())
                .description(request.getDescription())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .location(request.getLocation())
                .jobType(request.getJobType())
                .experienceLevel(request.getExperienceLevel())
                .expiredAt(request.getExpiredAt())
                .status(JobStatus.PENDING_APPROVAL) // BR5: always PENDING_APPROVAL
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            jobPost.setCategory(category);
        }

        jobPost = jobPostRepository.save(jobPost);

        // Add skills
        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            List<JobSkill> jobSkills = new ArrayList<>();
            for (JobSkillRequest skillReq : request.getSkills()) {
                Skill skill = skillRepository.findById(skillReq.getSkillId())
                        .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", skillReq.getSkillId()));
                JobSkill jobSkill = JobSkill.builder()
                        .jobPost(jobPost)
                        .skill(skill)
                        .level(skillReq.getLevel())
                        .build();
                jobSkills.add(jobSkill);
            }
            jobPost.setJobSkills(jobSkills);
            jobPost = jobPostRepository.save(jobPost);
        }

        return mapToResponse(jobPost);
    }

    @Override
    @Transactional
    public JobPostResponse updateJob(Long employerUserId, Long jobId, JobPostRequest request) {
        Employer employer = employerRepository.findByUserId(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found for user: " + employerUserId));

        JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("JobPost", "id", jobId));

        // BR12: Employer can only edit their own jobs
        if (!jobPost.getEmployer().getId().equals(employer.getId())) {
            throw new UnauthorizedException("You don't have permission to update this job post");
        }

        if (jobPost.getStatus() == JobStatus.CLOSED) {
            throw new BadRequestException("Cannot update a closed job post");
        }

        jobPost.setTitle(request.getTitle());
        jobPost.setDescription(request.getDescription());
        jobPost.setSalaryMin(request.getSalaryMin());
        jobPost.setSalaryMax(request.getSalaryMax());
        jobPost.setLocation(request.getLocation());
        jobPost.setJobType(request.getJobType());
        jobPost.setExperienceLevel(request.getExperienceLevel());
        jobPost.setExpiredAt(request.getExpiredAt());
        // Reset to pending approval after edit
        jobPost.setStatus(JobStatus.PENDING_APPROVAL);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            jobPost.setCategory(category);
        }

        // Update skills
        if (request.getSkills() != null) {
            jobPost.getJobSkills().clear();
            for (JobSkillRequest skillReq : request.getSkills()) {
                Skill skill = skillRepository.findById(skillReq.getSkillId())
                        .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", skillReq.getSkillId()));
                JobSkill jobSkill = JobSkill.builder()
                        .jobPost(jobPost)
                        .skill(skill)
                        .level(skillReq.getLevel())
                        .build();
                jobPost.getJobSkills().add(jobSkill);
            }
        }

        jobPost = jobPostRepository.save(jobPost);
        return mapToResponse(jobPost);
    }

    @Override
    @Transactional
    public void closeJob(Long employerUserId, Long jobId) {
        Employer employer = employerRepository.findByUserId(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found for user: " + employerUserId));

        JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("JobPost", "id", jobId));

        // BR12: Employer can only close their own jobs
        if (!jobPost.getEmployer().getId().equals(employer.getId())) {
            throw new UnauthorizedException("You don't have permission to close this job post");
        }

        // BR13: Soft delete - set status to CLOSED
        jobPost.setStatus(JobStatus.CLOSED);
        jobPostRepository.save(jobPost);
    }

    @Override
    public JobPostResponse getJobById(Long jobId) {
        JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("JobPost", "id", jobId));
        return mapToResponse(jobPost);
    }

    @Override
    public Page<JobPostResponse> getAllActiveJobs(Pageable pageable) {
        return jobPostRepository.findByStatus(JobStatus.ACTIVE, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<JobPostResponse> searchJobs(String keyword, Pageable pageable) {
        return jobPostRepository.searchByKeyword(keyword, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<JobPostResponse> getJobsByCategory(Long categoryId, Pageable pageable) {
        return jobPostRepository.findByCategoryId(categoryId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public List<JobPostResponse> getJobsByEmployer(Long employerUserId) {
        Employer employer = employerRepository.findByUserId(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found for user: " + employerUserId));
        return jobPostRepository.findByEmployerId(employer.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobPostResponse> getJobFeed(Long candidateUserId, Pageable pageable) {
        // Priority: jobs from followed employers first
        List<JobPost> followedJobs = jobPostRepository.findJobsFromFollowedEmployers(candidateUserId);
        List<Long> followedJobIds = followedJobs.stream().map(JobPost::getId).collect(Collectors.toList());

        // Then fill with other active jobs
        Page<JobPost> allActiveJobs = jobPostRepository.findActiveFeed(pageable);
        List<JobPost> otherJobs = allActiveJobs.getContent().stream()
                .filter(j -> !followedJobIds.contains(j.getId()))
                .collect(Collectors.toList());

        List<JobPost> feed = new ArrayList<>(followedJobs);
        feed.addAll(otherJobs);

        return feed.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<JobPostResponse> getPendingJobs() {
        return jobPostRepository.findByStatus(JobStatus.PENDING_APPROVAL).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private JobPostResponse mapToResponse(JobPost jobPost) {
        List<SkillResponse> skills = jobPost.getJobSkills().stream()
                .map(js -> SkillResponse.builder()
                        .id(js.getSkill().getId())
                        .name(js.getSkill().getName())
                        .level(js.getLevel())
                        .build())
                .collect(Collectors.toList());

        long appCount = applicationRepository.countByJobPostId(jobPost.getId());

        return JobPostResponse.builder()
                .id(jobPost.getId())
                .title(jobPost.getTitle())
                .description(jobPost.getDescription())
                .salaryMin(jobPost.getSalaryMin())
                .salaryMax(jobPost.getSalaryMax())
                .location(jobPost.getLocation())
                .jobType(jobPost.getJobType())
                .experienceLevel(jobPost.getExperienceLevel())
                .status(jobPost.getStatus())
                .postedAt(jobPost.getPostedAt())
                .expiredAt(jobPost.getExpiredAt())
                .rejectionReason(jobPost.getRejectionReason())
                .employerId(jobPost.getEmployer().getId())
                .companyName(jobPost.getEmployer().getCompanyName())
                .logoUrl(jobPost.getEmployer().getLogoUrl())
                .categoryId(jobPost.getCategory() != null ? jobPost.getCategory().getId() : null)
                .categoryName(jobPost.getCategory() != null ? jobPost.getCategory().getName() : null)
                .skills(skills)
                .applicationCount((int) appCount)
                .build();
    }
}
