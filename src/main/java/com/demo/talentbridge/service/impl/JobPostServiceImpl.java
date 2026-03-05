package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.request.JobPostRequest;
import com.demo.talentbridge.dto.request.JobSkillRequest;
import com.demo.talentbridge.dto.response.JobPostResponse;
import com.demo.talentbridge.dto.response.SkillResponse;
import com.demo.talentbridge.entity.*;
import com.demo.talentbridge.enums.JobStatus;
import com.demo.talentbridge.enums.NotificationType;
import com.demo.talentbridge.enums.SkillName;
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

import java.time.LocalDateTime;
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
    private ApplicationRepository applicationRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public JobPostResponse createJob(Long employerUserId, JobPostRequest request) {
        Employer employer = employerRepository.findByUserId(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found for user: " + employerUserId));

        validateSalaryRange(request);

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
                .status(JobStatus.PENDING_APPROVAL)
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            jobPost.setCategory(category);
        }

        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            List<JobSkill> jobSkills = buildJobSkills(request.getSkills(), jobPost);
            jobPost.setJobSkills(jobSkills);
        }

        jobPost = jobPostRepository.save(jobPost);
        return mapToResponse(jobPost);
    }

    @Override
    @Transactional
    public JobPostResponse updateJob(Long employerUserId, Long jobId, JobPostRequest request) {
        Employer employer = employerRepository.findByUserId(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found for user: " + employerUserId));

        JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("JobPost", "id", jobId));

        if (!jobPost.getEmployer().getId().equals(employer.getId())) {
            throw new UnauthorizedException("You don't have permission to update this job post");
        }

        if (jobPost.getStatus() == JobStatus.CLOSED) {
            throw new BadRequestException("Cannot update a closed job post");
        }

        validateSalaryRange(request);

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
        } else {
            jobPost.setCategory(null);
        }

        if (request.getSkills() != null) {
            jobPost.getJobSkills().clear();
            List<JobSkill> newSkills = buildJobSkills(request.getSkills(), jobPost);
            jobPost.getJobSkills().addAll(newSkills);
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

        if (!jobPost.getEmployer().getId().equals(employer.getId())) {
            throw new UnauthorizedException("You don't have permission to close this job post");
        }

        if (jobPost.getStatus() == JobStatus.CLOSED) {
            throw new BadRequestException("Job post is already closed");
        }

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
        // Validate category exists
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        return jobPostRepository.findByCategoryId(categoryId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public Page<JobPostResponse> getJobsBySkills(List<SkillName> skillNames, Pageable pageable) {
        if (skillNames == null || skillNames.isEmpty()) {
            return jobPostRepository.findByStatus(JobStatus.ACTIVE, pageable).map(this::mapToResponse);
        }
        return jobPostRepository.findBySkillNames(skillNames, pageable).map(this::mapToResponse);
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

    @Override
    @Transactional
    public JobPostResponse approveJob(Long jobId) {
        JobPost job = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("JobPost", "id", jobId));

        if (job.getStatus() != JobStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Job is not in PENDING_APPROVAL status");
        }

        job.setStatus(JobStatus.ACTIVE);
        job.setRejectionReason(null);
        job = jobPostRepository.save(job);

        // Notify followers of new job
        notificationService.notifyFollowersOfNewJob(
                job.getEmployer().getUser().getId(), job.getId(), job.getTitle());

        // Notify employer
        notificationService.createNotification(
                job.getEmployer().getUser(),
                "Job Post Approved",
                "Your job post \"" + job.getTitle() + "\" has been approved and is now active.",
                NotificationType.SYSTEM,
                "/jobs/" + job.getId()
        );

        return mapToResponse(job);
    }

    @Override
    @Transactional
    public JobPostResponse rejectJob(Long jobId, String rejectionReason) {
        JobPost job = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("JobPost", "id", jobId));

        if (job.getStatus() != JobStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Job is not in PENDING_APPROVAL status");
        }

        job.setStatus(JobStatus.REJECTED);
        job.setRejectionReason(rejectionReason);
        job = jobPostRepository.save(job);

        // Notify employer
        notificationService.createNotification(
                job.getEmployer().getUser(),
                "Job Post Rejected",
                "Your job post \"" + job.getTitle() + "\" has been rejected. Reason: " + rejectionReason,
                NotificationType.SYSTEM,
                "/jobs/" + job.getId()
        );

        return mapToResponse(job);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private List<JobSkill> buildJobSkills(List<JobSkillRequest> skillRequests, JobPost jobPost) {
        return skillRequests.stream()
                .map(req -> JobSkill.builder()
                        .jobPost(jobPost)
                        .skillName(req.getSkillName())
                        .level(req.getLevel())
                        .build())
                .collect(Collectors.toList());
    }

    private void validateSalaryRange(JobPostRequest request) {
        if (request.getSalaryMin() != null && request.getSalaryMax() != null
                && request.getSalaryMin().compareTo(request.getSalaryMax()) > 0) {
            throw new BadRequestException("Salary min cannot be greater than salary max");
        }
    }

    private JobPostResponse mapToResponse(JobPost jobPost) {
        List<SkillResponse> skills = jobPost.getJobSkills().stream()
                .map(js -> SkillResponse.builder()
                        .skillName(js.getSkillName())
                        .displayName(formatSkillName(js.getSkillName()))
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

    /**
     * Converts enum name to a human-readable display name.
     * e.g. SPRING_BOOT -> "Spring Boot", NODEJS -> "Node.js"
     */
    private String formatSkillName(SkillName skillName) {
        if (skillName == null) return null;
        return switch (skillName) {
            case NODEJS -> "Node.js";
            case NEXTJS -> "Next.js";
            case NUXTJS -> "Nuxt.js";
            case NESTJS -> "NestJS";
            case CSHARP -> "C#";
            case CPP -> "C++";
            case GRAPHQL -> "GraphQL";
            case GRPC -> "gRPC";
            case GITHUB_ACTIONS -> "GitHub Actions";
            case GITLAB_CI -> "GitLab CI";
            case TAILWIND_CSS -> "Tailwind CSS";
            case SPRING_BOOT -> "Spring Boot";
            case SPRING_FRAMEWORK -> "Spring Framework";
            case REACT_NATIVE -> "React Native";
            case ASP_NET -> "ASP.NET";
            case MACHINE_LEARNING -> "Machine Learning";
            case DEEP_LEARNING -> "Deep Learning";
            case DATA_SCIENCE -> "Data Science";
            case DATA_ANALYSIS -> "Data Analysis";
            case POWER_BI -> "Power BI";
            case UI_UX_DESIGN -> "UI/UX Design";
            case REST_API -> "REST API";
            case PROJECT_MANAGEMENT -> "Project Management";
            case BUSINESS_ANALYSIS -> "Business Analysis";
            case PROBLEM_SOLVING -> "Problem Solving";
            default -> capitalize(skillName.name().replace("_", " "));
        };
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] words = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
