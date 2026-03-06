package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.request.ApplicationRequest;
import com.demo.talentbridge.dto.request.UpdateApplicationStatusRequest;
import com.demo.talentbridge.dto.response.ApplicationHistoryResponse;
import com.demo.talentbridge.dto.response.ApplicationResponse;
import com.demo.talentbridge.entity.*;
import com.demo.talentbridge.enums.ApplicationStatus;
import com.demo.talentbridge.enums.JobStatus;
import com.demo.talentbridge.enums.NotificationType;
import com.demo.talentbridge.exception.BadRequestException;
import com.demo.talentbridge.exception.DuplicateResourceException;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.exception.UnauthorizedException;
import com.demo.talentbridge.repository.*;
import com.demo.talentbridge.service.ApplicationService;
import com.demo.talentbridge.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationHistoryRepository applicationHistoryRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private JobPostRepository jobPostRepository;

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public ApplicationResponse applyForJob(Long candidateUserId, ApplicationRequest request) {
        Candidate candidate = candidateRepository.findByUserId(candidateUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + candidateUserId));

        JobPost jobPost = jobPostRepository.findById(request.getJobPostId())
                .orElseThrow(() -> new ResourceNotFoundException("JobPost", "id", request.getJobPostId()));

        if (jobPost.getStatus() != JobStatus.ACTIVE) {
            throw new BadRequestException("This job is not accepting applications");
        }

        // BR7: Prevent duplicate applications
        if (applicationRepository.existsByCandidateIdAndJobPostId(candidate.getId(), jobPost.getId())) {
            throw new DuplicateResourceException("You have already applied for this job");
        }

        Application application = Application.builder()
                .candidate(candidate)
                .jobPost(jobPost)
                .status(ApplicationStatus.SUBMITTED)
                .cvUrlAtTime(request.getCvUrlAtTime()) // BR: Snapshot CV URL
                .coverLetter(request.getCoverLetter())
                .build();

        application = applicationRepository.save(application);

        // Create initial history entry
        ApplicationHistory history = ApplicationHistory.builder()
                .application(application)
                .changedBy(candidate.getUser())
                .fromStatus(ApplicationStatus.SUBMITTED)
                .toStatus(ApplicationStatus.SUBMITTED)
                .note("Application submitted")
                .build();
        applicationHistoryRepository.save(history);

        // Notify employer
        notificationService.createNotification(
                jobPost.getEmployer().getUser(),
                "New Application Received",
                candidate.getUser().getFullName() + " applied for " + jobPost.getTitle(),
                NotificationType.APPLICATION_STATUS_CHANGED,
                "/applications/" + application.getId()
        );

        return mapToResponse(application);
    }

    @Override
    @Transactional
    public ApplicationResponse updateStatus(Long employerUserId, Long applicationId, UpdateApplicationStatusRequest request) {
        Employer employer = employerRepository.findByUserId(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found for user: " + employerUserId));

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));

        // Verify employer owns this job
        if (!application.getJobPost().getEmployer().getId().equals(employer.getId())) {
            throw new UnauthorizedException("You don't have permission to update this application");
        }

        ApplicationStatus oldStatus = application.getStatus();
        ApplicationStatus newStatus = request.getStatus();

        application.setStatus(newStatus);
        application = applicationRepository.save(application);

        // Auto-insert ApplicationHistory on every status change
        User changedBy = userRepository.findById(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", employerUserId));

        ApplicationHistory history = ApplicationHistory.builder()
                .application(application)
                .changedBy(changedBy)
                .fromStatus(oldStatus)
                .toStatus(newStatus)
                .note(request.getNote())
                .build();
        applicationHistoryRepository.save(history);

        // Notify candidate
        notificationService.createNotification(
                application.getCandidate().getUser(),
                "Application Status Updated",
                "Your application for " + application.getJobPost().getTitle() + " is now " + newStatus.name(),
                NotificationType.APPLICATION_STATUS_CHANGED,
                "/applications/" + application.getId()
        );

        return mapToResponse(application);
    }

    @Override
    public ApplicationResponse getApplicationById(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));
        return mapToResponse(application);
    }

    @Override
    public List<ApplicationResponse> getApplicationsByCandidate(Long candidateUserId) {
        Candidate candidate = candidateRepository.findByUserId(candidateUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + candidateUserId));
        return applicationRepository.findByCandidateId(candidate.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationResponse> getApplicationsByJobPost(Long employerUserId, Long jobPostId) {
        Employer employer = employerRepository.findByUserId(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found for user: " + employerUserId));
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new ResourceNotFoundException("JobPost", "id", jobPostId));
        if (!jobPost.getEmployer().getId().equals(employer.getId())) {
            throw new UnauthorizedException("You don't have permission to view these applications");
        }
        return applicationRepository.findByJobPostId(jobPostId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationResponse> getApplicationsByEmployer(Long employerUserId) {
        Employer employer = employerRepository.findByUserId(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found for user: " + employerUserId));
        return applicationRepository.findByEmployerId(employer.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void withdrawApplication(Long candidateUserId, Long applicationId) {
        Candidate candidate = candidateRepository.findByUserId(candidateUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + candidateUserId));
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", applicationId));
        if (!application.getCandidate().getId().equals(candidate.getId())) {
            throw new UnauthorizedException("You don't have permission to withdraw this application");
        }
        if (application.getStatus() == ApplicationStatus.OFFERED || application.getStatus() == ApplicationStatus.REJECTED) {
            throw new BadRequestException("Cannot withdraw application in current status");
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);

        ApplicationHistory history = ApplicationHistory.builder()
                .application(application)
                .changedBy(candidate.getUser())
                .fromStatus(oldStatus)
                .toStatus(ApplicationStatus.WITHDRAWN)
                .note("Application withdrawn by candidate")
                .build();
        applicationHistoryRepository.save(history);
    }

    private ApplicationResponse mapToResponse(Application application) {
        List<ApplicationHistoryResponse> histories = applicationHistoryRepository
                .findByApplicationIdOrderByChangedAtAsc(application.getId())
                .stream()
                .map(h -> ApplicationHistoryResponse.builder()
                        .id(h.getId())
                        .fromStatus(h.getFromStatus())
                        .toStatus(h.getToStatus())
                        .note(h.getNote())
                        .changedByUsername(h.getChangedBy() != null ? h.getChangedBy().getUsername() : null)
                        .changedAt(h.getChangedAt())
                        .build())
                .collect(Collectors.toList());

        return ApplicationResponse.builder()
                .id(application.getId())
                .status(application.getStatus())
                .cvUrlAtTime(application.getCvUrlAtTime())
                .coverLetter(application.getCoverLetter())
                .appliedAt(application.getAppliedAt())
                .jobPostId(application.getJobPost().getId())
                .jobTitle(application.getJobPost().getTitle())
                .companyName(application.getJobPost().getEmployer().getCompanyName())
                .candidateId(application.getCandidate().getId())
                .candidateName(application.getCandidate().getUser().getFullName())
                .candidateEmail(application.getCandidate().getUser().getEmail())
                .histories(histories)
                .build();
    }
}
