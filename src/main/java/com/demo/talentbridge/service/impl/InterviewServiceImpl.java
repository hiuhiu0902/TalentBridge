package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.request.InterviewRequest;
import com.demo.talentbridge.dto.response.InterviewResponse;
import com.demo.talentbridge.entity.*;
import com.demo.talentbridge.enums.ApplicationStatus;
import com.demo.talentbridge.enums.InterviewStatus;
import com.demo.talentbridge.enums.NotificationType;
import com.demo.talentbridge.exception.BadRequestException;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.exception.UnauthorizedException;
import com.demo.talentbridge.repository.*;
import com.demo.talentbridge.service.InterviewService;
import com.demo.talentbridge.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterviewServiceImpl implements InterviewService {

    @Autowired private InterviewRepository interviewRepository;
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private EmployerRepository employerRepository;
    @Autowired private CandidateRepository candidateRepository;
    @Autowired private NotificationService notificationService;

    @Override
    @Transactional
    public InterviewResponse scheduleInterview(Long employerUserId, InterviewRequest request) {
        Employer employer = employerRepository.findByUserId(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found for user: " + employerUserId));

        Application app = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", request.getApplicationId()));

        if (!app.getJobPost().getEmployer().getId().equals(employer.getId())) {
            throw new UnauthorizedException("You don't have permission to schedule interview for this application");
        }

        if (app.getStatus() == ApplicationStatus.REJECTED || app.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new BadRequestException("Cannot schedule interview for application in status: " + app.getStatus());
        }

        if (request.getInterviewAt() == null) {
            throw new BadRequestException("Interview date/time is required");
        }

        Interview interview = Interview.builder()
                .application(app)
                .interviewAt(request.getInterviewAt())
                .location(request.getLocation())
                .meetingLink(request.getMeetingLink())
                .note(request.getNote())
                .status(InterviewStatus.SCHEDULED)
                .build();

        interview = interviewRepository.save(interview);

        // Auto-update application status to INTERVIEW
        if (app.getStatus() == ApplicationStatus.SUBMITTED || app.getStatus() == ApplicationStatus.REVIEWING) {
            app.setStatus(ApplicationStatus.INTERVIEW);
            applicationRepository.save(app);
        }

        notificationService.createNotification(
                app.getCandidate().getUser(),
                "Interview Scheduled",
                "You have an interview scheduled for \"" + app.getJobPost().getTitle() + "\" on " + request.getInterviewAt(),
                NotificationType.INTERVIEW_SCHEDULED,
                "/applications/" + app.getId()
        );

        return map(interview);
    }

    @Override
    @Transactional
    public InterviewResponse updateInterview(Long employerUserId, Long interviewId, InterviewRequest request) {
        Employer employer = employerRepository.findByUserId(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found for user: " + employerUserId));

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", interviewId));

        if (!interview.getApplication().getJobPost().getEmployer().getId().equals(employer.getId())) {
            throw new UnauthorizedException("You don't have permission to update this interview");
        }

        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new BadRequestException("Cannot update a cancelled interview");
        }

        interview.setInterviewAt(request.getInterviewAt());
        interview.setLocation(request.getLocation());
        interview.setMeetingLink(request.getMeetingLink());
        interview.setNote(request.getNote());
        interview.setStatus(InterviewStatus.RESCHEDULED);
        interview = interviewRepository.save(interview);

        notificationService.createNotification(
                interview.getApplication().getCandidate().getUser(),
                "Interview Rescheduled",
                "Your interview for \"" + interview.getApplication().getJobPost().getTitle() + "\" has been rescheduled to " + request.getInterviewAt(),
                NotificationType.INTERVIEW_SCHEDULED,
                "/applications/" + interview.getApplication().getId()
        );

        return map(interview);
    }

    @Override
    @Transactional
    public InterviewResponse updateInterviewStatus(Long employerUserId, Long interviewId, InterviewStatus status) {
        Employer employer = employerRepository.findByUserId(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found for user: " + employerUserId));

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", interviewId));

        if (!interview.getApplication().getJobPost().getEmployer().getId().equals(employer.getId())) {
            throw new UnauthorizedException("You don't have permission to update this interview");
        }

        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new BadRequestException("Cannot update a cancelled interview");
        }

        interview.setStatus(status);
        return map(interviewRepository.save(interview));
    }

    @Override
    @Transactional
    public void cancelInterview(Long employerUserId, Long interviewId) {
        Employer employer = employerRepository.findByUserId(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found for user: " + employerUserId));

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", interviewId));

        if (!interview.getApplication().getJobPost().getEmployer().getId().equals(employer.getId())) {
            throw new UnauthorizedException("You don't have permission to cancel this interview");
        }

        interview.setStatus(InterviewStatus.CANCELLED);
        interviewRepository.save(interview);

        notificationService.createNotification(
                interview.getApplication().getCandidate().getUser(),
                "Interview Cancelled",
                "Your interview for \"" + interview.getApplication().getJobPost().getTitle() + "\" has been cancelled.",
                NotificationType.INTERVIEW_SCHEDULED,
                "/applications/" + interview.getApplication().getId()
        );
    }

    @Override
    public List<InterviewResponse> getInterviewsByApplication(Long applicationId) {
        return interviewRepository.findByApplicationId(applicationId).stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    @Override
    public List<InterviewResponse> getInterviewsForCandidate(Long candidateUserId) {
        return interviewRepository.findByApplicationCandidateUserId(candidateUserId).stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    @Override
    public List<InterviewResponse> getInterviewsForEmployer(Long employerUserId) {
        return interviewRepository.findByApplicationJobPostEmployerUserId(employerUserId).stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    private InterviewResponse map(Interview i) {
        return InterviewResponse.builder()
                .id(i.getId())
                .applicationId(i.getApplication().getId())
                .jobTitle(i.getApplication().getJobPost().getTitle())
                .candidateName(i.getApplication().getCandidate().getFullName())
                .interviewAt(i.getInterviewAt())
                .location(i.getLocation())
                .meetingLink(i.getMeetingLink())
                .note(i.getNote())
                .status(i.getStatus())
                .createdAt(i.getCreatedAt())
                .build();
    }
}
