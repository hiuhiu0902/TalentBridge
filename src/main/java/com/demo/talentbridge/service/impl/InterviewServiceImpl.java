package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.request.InterviewRequest;
import com.demo.talentbridge.dto.response.InterviewResponse;
import com.demo.talentbridge.entity.*;
import com.demo.talentbridge.enums.InterviewStatus;
import com.demo.talentbridge.enums.NotificationType;
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
    @Autowired private NotificationService notificationService;

    @Override @Transactional
    public InterviewResponse scheduleInterview(Long employerUserId, InterviewRequest request) {
        Employer employer = employerRepository.findByUserId(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));
        Application app = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application","id",request.getApplicationId()));
        if (!app.getJobPost().getEmployer().getId().equals(employer.getId()))
            throw new UnauthorizedException("Not authorized");
        Interview interview = Interview.builder()
                .application(app).interviewAt(request.getInterviewAt())
                .location(request.getLocation()).meetingLink(request.getMeetingLink())
                .note(request.getNote()).status(InterviewStatus.SCHEDULED).build();
        interview = interviewRepository.save(interview);
        notificationService.createNotification(app.getCandidate().getUser(),
                "Interview Scheduled", "Interview for " + app.getJobPost().getTitle() + " scheduled",
                NotificationType.INTERVIEW_SCHEDULED, "/applications/" + app.getId());
        return map(interview);
    }

    @Override @Transactional
    public InterviewResponse updateInterviewStatus(Long employerUserId, Long interviewId, InterviewStatus status) {
        Employer employer = employerRepository.findByUserId(employerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer not found"));
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview","id",interviewId));
        if (!interview.getApplication().getJobPost().getEmployer().getId().equals(employer.getId()))
            throw new UnauthorizedException("Not authorized");
        interview.setStatus(status);
        return map(interviewRepository.save(interview));
    }

    @Override
    public List<InterviewResponse> getInterviewsByApplication(Long applicationId) {
        return interviewRepository.findByApplicationId(applicationId).stream().map(this::map).collect(Collectors.toList());
    }

    private InterviewResponse map(Interview i) {
        return InterviewResponse.builder().id(i.getId())
                .applicationId(i.getApplication().getId())
                .interviewAt(i.getInterviewAt()).location(i.getLocation())
                .meetingLink(i.getMeetingLink()).note(i.getNote())
                .status(i.getStatus()).createdAt(i.getCreatedAt()).build();
    }
}
