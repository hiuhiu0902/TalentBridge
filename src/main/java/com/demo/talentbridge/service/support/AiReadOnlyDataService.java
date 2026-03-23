package com.demo.talentbridge.service.support;

import com.demo.talentbridge.entity.Application;
import com.demo.talentbridge.entity.Candidate;
import com.demo.talentbridge.entity.Employer;
import com.demo.talentbridge.entity.JobPost;
import com.demo.talentbridge.entity.Notification;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.enums.ApplicationStatus;
import com.demo.talentbridge.enums.JobStatus;
import com.demo.talentbridge.enums.UserRole;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.exception.UnauthorizedException;
import com.demo.talentbridge.repository.ApplicationRepository;
import com.demo.talentbridge.repository.CandidateRepository;
import com.demo.talentbridge.repository.CategoryRepository;
import com.demo.talentbridge.repository.EmployerRepository;
import com.demo.talentbridge.repository.JobPostRepository;
import com.demo.talentbridge.repository.NotificationRepository;
import com.demo.talentbridge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.demo.talentbridge.dto.ai.JobMatchResult;
import com.demo.talentbridge.dto.ai.ProfileGapResult;
import org.springframework.data.domain.PageRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AiReadOnlyDataService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CandidateRepository candidateRepository;
    @Autowired
    private EmployerRepository employerRepository;
    @Autowired
    private JobPostRepository jobPostRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private JobMatchingService jobMatchingService;
    @Autowired
    private ProfileGapAnalysisService profileGapAnalysisService;
    @Autowired
    private CandidateRankingService candidateRankingService;

    public Map<String, Object> getPlatformOverview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalCandidates", candidateRepository.count());
        result.put("totalEmployers", employerRepository.count());
        result.put("totalCategories", categoryRepository.count());
        result.put("totalActiveJobs", jobPostRepository.countByStatus(JobStatus.ACTIVE));
        result.put("totalPendingJobs", jobPostRepository.countByStatus(JobStatus.PENDING_APPROVAL));
        result.put("totalClosedJobs", jobPostRepository.countByStatus(JobStatus.CLOSED));
        result.put("totalApplications", applicationRepository.count());
        return result;
    }

    public Map<String, Object> searchPublicJobs(String keyword, Integer limit) {
        int safeLimit = clampLimit(limit, 10);
        List<JobPost> jobs = (keyword == null || keyword.isBlank())
                ? jobPostRepository.findByStatus(JobStatus.ACTIVE, PageRequest.of(0, safeLimit)).getContent()
                : jobPostRepository.searchByKeyword(keyword.trim(), PageRequest.of(0, safeLimit)).getContent();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", jobs.size());
        result.put("jobs", jobs.stream().map(this::toPublicJobMap).collect(Collectors.toList()));
        return result;
    }

    public Map<String, Object> getMyCandidateProfile(Long userId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + userId));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", candidate.getId());
        result.put("fullName", candidate.getUser().getFullName());
        result.put("email", candidate.getUser().getEmail());
        result.put("phone", candidate.getPhone());
        result.put("address", candidate.getAddress());
        result.put("summary", candidate.getSummary());
        result.put("cvUrl", candidate.getCvUrl());
        result.put("skills", candidate.getCandidateSkills().stream().map(skill -> {
            Map<String, Object> skillMap = new LinkedHashMap<>();
            skillMap.put("skillName", skill.getSkillName() != null ? skill.getSkillName().name() : null);
            skillMap.put("level", skill.getLevel() != null ? skill.getLevel().name() : null);
            return skillMap;
        }).collect(Collectors.toList()));
        result.put("educations", candidate.getEducations().stream().map(education -> {
            Map<String, Object> educationMap = new LinkedHashMap<>();
            educationMap.put("school", education.getSchool());
            educationMap.put("major", education.getMajor());
            educationMap.put("degree", education.getDegree());
            educationMap.put("startDate", education.getStartDate());
            educationMap.put("endDate", education.getEndDate());
            return educationMap;
        }).collect(Collectors.toList()));
        result.put("workExperiences", candidate.getWorkExperiences().stream().map(work -> {
            Map<String, Object> workMap = new LinkedHashMap<>();
            workMap.put("company", work.getCompany());
            workMap.put("position", work.getPosition());
            workMap.put("startDate", work.getStartDate());
            workMap.put("endDate", work.getEndDate());
            workMap.put("currentlyWorking", work.getCurrentlyWorking());
            return workMap;
        }).collect(Collectors.toList()));
        return result;
    }

    public Map<String, Object> getMyApplications(Long userId, String status, Integer limit) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + userId));

        ApplicationStatus filterStatus = parseApplicationStatus(status);
        List<Application> applications = applicationRepository.findByCandidateId(candidate.getId());
        List<Application> filtered = applications.stream()
                .filter(application -> filterStatus == null || application.getStatus() == filterStatus)
                .limit(clampLimit(limit, 20))
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", filtered.size());
        result.put("applications", filtered.stream().map(this::toApplicationSummary).collect(Collectors.toList()));
        return result;
    }

    public Map<String, Object> getMyEmployerProfile(Long userId) {
        Employer employer = employerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found for user: " + userId));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", employer.getId());
        result.put("companyName", employer.getCompanyName());
        result.put("website", employer.getWebsite());
        result.put("description", employer.getDescription());
        result.put("logoUrl", employer.getLogoUrl());
        result.put("industry", employer.getIndustry());
        result.put("companySize", employer.getCompanySize());
        result.put("address", employer.getAddress());
        result.put("email", employer.getUser().getEmail());
        result.put("contactName", employer.getUser().getFullName());
        result.put("totalJobs", jobPostRepository.countByEmployerId(employer.getId()));
        return result;
    }

    public Map<String, Object> getMyJobPosts(Long userId, String status, Integer limit) {
        Employer employer = employerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found for user: " + userId));

        JobStatus filterStatus = parseJobStatus(status);
        List<JobPost> jobs = jobPostRepository.findByEmployerId(employer.getId()).stream()
                .filter(job -> filterStatus == null || job.getStatus() == filterStatus)
                .limit(clampLimit(limit, 20))
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", jobs.size());
        result.put("jobs", jobs.stream().map(this::toEmployerJobMap).collect(Collectors.toList()));
        return result;
    }

    public Map<String, Object> getApplicationsForMyJob(Long userId, Long jobPostId, String status) {
        Employer employer = employerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found for user: " + userId));

        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new ResourceNotFoundException("JobPost", "id", jobPostId));

        if (!jobPost.getEmployer().getId().equals(employer.getId())) {
            throw new UnauthorizedException("You don't have permission to view applications for this job");
        }

        ApplicationStatus filterStatus = parseApplicationStatus(status);
        List<Application> applications = applicationRepository.findByJobPostId(jobPostId).stream()
                .filter(application -> filterStatus == null || application.getStatus() == filterStatus)
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("jobPostId", jobPost.getId());
        result.put("jobTitle", jobPost.getTitle());
        result.put("count", applications.size());
        result.put("applications", applications.stream().map(this::toApplicationSummary).collect(Collectors.toList()));
        return result;
    }

    public Map<String, Object> getMyNotifications(Long userId, Boolean unreadOnly, Integer limit) {
        List<Notification> notifications = Boolean.TRUE.equals(unreadOnly)
                ? notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<Notification> limited = notifications.stream()
                .limit(clampLimit(limit, 20))
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("unreadCount", notificationRepository.countByUserIdAndIsReadFalse(userId));
        result.put("notifications", limited.stream().map(notification -> {
            Map<String, Object> notificationMap = new LinkedHashMap<>();
            notificationMap.put("id", notification.getId());
            notificationMap.put("title", notification.getTitle());
            notificationMap.put("content", notification.getContent());
            notificationMap.put("type", notification.getType() != null ? notification.getType().name() : null);
            notificationMap.put("referenceUrl", notification.getReferenceUrl());
            notificationMap.put("isRead", notification.getIsRead());
            notificationMap.put("createdAt", notification.getCreatedAt());
            return notificationMap;
        }).collect(Collectors.toList()));
        return result;
    }

    public Map<String, Object> getAdminOverview(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Admin access required");
        }

        return new LinkedHashMap<>(getPlatformOverview());
    }

    private Map<String, Object> toPublicJobMap(JobPost jobPost) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", jobPost.getId());
        result.put("title", jobPost.getTitle());
        result.put("companyName", jobPost.getEmployer().getCompanyName());
        result.put("location", jobPost.getLocation());
        result.put("jobType", jobPost.getJobType());
        result.put("experienceLevel", jobPost.getExperienceLevel());
        result.put("salaryMin", jobPost.getSalaryMin());
        result.put("salaryMax", jobPost.getSalaryMax());
        result.put("categoryName", jobPost.getCategory() != null ? jobPost.getCategory().getName() : null);
        result.put("skills", jobPost.getJobSkills().stream()
                .map(skill -> skill.getSkillName() != null ? skill.getSkillName().name() : null)
                .collect(Collectors.toList()));
        result.put("postedAt", jobPost.getPostedAt());
        result.put("expiredAt", jobPost.getExpiredAt());
        result.put("status", jobPost.getStatus() != null ? jobPost.getStatus().name() : null);
        return result;
    }

    private Map<String, Object> toEmployerJobMap(JobPost jobPost) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", jobPost.getId());
        result.put("title", jobPost.getTitle());
        result.put("status", jobPost.getStatus() != null ? jobPost.getStatus().name() : null);
        result.put("location", jobPost.getLocation());
        result.put("jobType", jobPost.getJobType());
        result.put("experienceLevel", jobPost.getExperienceLevel());
        result.put("salaryMin", jobPost.getSalaryMin());
        result.put("salaryMax", jobPost.getSalaryMax());
        result.put("applicationCount", applicationRepository.countByJobPostId(jobPost.getId()));
        result.put("postedAt", jobPost.getPostedAt());
        result.put("expiredAt", jobPost.getExpiredAt());
        return result;
    }

    private Map<String, Object> toApplicationSummary(Application application) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", application.getId());
        result.put("status", application.getStatus() != null ? application.getStatus().name() : null);
        result.put("candidateName", application.getCandidate().getUser().getFullName());
        result.put("candidateEmail", application.getCandidate().getUser().getEmail());
        result.put("jobPostId", application.getJobPost().getId());
        result.put("jobTitle", application.getJobPost().getTitle());
        result.put("companyName", application.getJobPost().getEmployer().getCompanyName());
        result.put("appliedAt", application.getAppliedAt());
        result.put("cvUrlAtTime", application.getCvUrlAtTime());
        result.put("coverLetter", application.getCoverLetter());
        return result;
    }

    private int clampLimit(Integer limit, int defaultValue) {
        if (limit == null) {
            return defaultValue;
        }
        return Math.max(1, Math.min(limit, 20));
    }

    private ApplicationStatus parseApplicationStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return ApplicationStatus.valueOf(status.trim().toUpperCase());
    }

    private JobStatus parseJobStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return JobStatus.valueOf(status.trim().toUpperCase());
    }
    public Map<String, Object> recommendJobsForMe(Long userId, Integer limit) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + userId));

        List<JobPost> jobs = jobPostRepository.findByStatus(JobStatus.ACTIVE, PageRequest.of(0, 50)).getContent();
        List<JobMatchResult> recommendedJobs = jobMatchingService.recommendJobsForCandidate(candidate, jobs, limit);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("candidateId", candidate.getId());
        result.put("candidateName", candidate.getUser() != null ? candidate.getUser().getFullName() : null);
        result.put("count", recommendedJobs.size());
        result.put("recommendedJobs", recommendedJobs);
        return result;
    }

    public Map<String, Object> analyzeMyProfileGap(Long userId, String keyword, Integer limit) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + userId));

        ProfileGapResult analysis = profileGapAnalysisService.analyzeProfileGap(candidate, keyword, limit);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("candidateId", candidate.getId());
        result.put("candidateName", candidate.getUser() != null ? candidate.getUser().getFullName() : null);
        result.put("analysis", analysis);
        return result;
    }

    public Map<String, Object> recommendCandidatesForMyJob(Long userId, Long jobPostId, Integer limit) {
        return candidateRankingService.recommendCandidatesForJob(userId, jobPostId, limit);
    }
}
