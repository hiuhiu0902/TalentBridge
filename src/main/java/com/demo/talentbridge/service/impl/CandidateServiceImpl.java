package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.request.CandidateProfileRequest;
import com.demo.talentbridge.dto.request.EducationRequest;
import com.demo.talentbridge.dto.request.WorkExperienceRequest;
import com.demo.talentbridge.dto.response.CandidateProfileResponse;
import com.demo.talentbridge.dto.response.EducationResponse;
import com.demo.talentbridge.dto.response.SkillResponse;
import com.demo.talentbridge.dto.response.WorkExperienceResponse;
import com.demo.talentbridge.entity.*;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.exception.UnauthorizedException;
import com.demo.talentbridge.repository.*;
import com.demo.talentbridge.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidateServiceImpl implements CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private EducationRepository educationRepository;

    @Autowired
    private WorkExperienceRepository workExperienceRepository;

    @Autowired
    private CandidateSkillRepository candidateSkillRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Override
    public CandidateProfileResponse getProfile(Long userId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + userId));
        return mapToResponse(candidate);
    }

    @Override
    @Transactional
    public CandidateProfileResponse updateProfile(Long userId, CandidateProfileRequest request) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user: " + userId));
        if (request.getFullName() != null) candidate.setFullName(request.getFullName());
        if (request.getPhone() != null) candidate.setPhone(request.getPhone());
        if (request.getAddress() != null) candidate.setAddress(request.getAddress());
        if (request.getSummary() != null) candidate.setSummary(request.getSummary());
        if (request.getCvUrl() != null) candidate.setCvUrl(request.getCvUrl());
        if (request.getAvatarUrl() != null) candidate.setAvatarUrl(request.getAvatarUrl());
        candidate = candidateRepository.save(candidate);
        return mapToResponse(candidate);
    }

    @Override
    @Transactional
    public EducationResponse addEducation(Long userId, EducationRequest request) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + userId));
        Education education = Education.builder()
                .candidate(candidate)
                .school(request.getSchool())
                .major(request.getMajor())
                .degree(request.getDegree())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .description(request.getDescription())
                .build();
        education = educationRepository.save(education);
        return mapEducationToResponse(education);
    }

    @Override
    @Transactional
    public EducationResponse updateEducation(Long userId, Long educationId, EducationRequest request) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + userId));
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education", "id", educationId));
        if (!education.getCandidate().getId().equals(candidate.getId())) {
            throw new UnauthorizedException("You don't have permission to update this education");
        }
        education.setSchool(request.getSchool());
        education.setMajor(request.getMajor());
        education.setDegree(request.getDegree());
        education.setStartDate(request.getStartDate());
        education.setEndDate(request.getEndDate());
        education.setDescription(request.getDescription());
        education = educationRepository.save(education);
        return mapEducationToResponse(education);
    }

    @Override
    @Transactional
    public void deleteEducation(Long userId, Long educationId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + userId));
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education", "id", educationId));
        if (!education.getCandidate().getId().equals(candidate.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this education");
        }
        educationRepository.delete(education);
    }

    @Override
    public List<EducationResponse> getEducations(Long userId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + userId));
        return educationRepository.findByCandidateId(candidate.getId()).stream()
                .map(this::mapEducationToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkExperienceResponse addWorkExperience(Long userId, WorkExperienceRequest request) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + userId));
        WorkExperience exp = WorkExperience.builder()
                .candidate(candidate)
                .company(request.getCompany())
                .position(request.getPosition())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .description(request.getDescription())
                .currentlyWorking(request.getCurrentlyWorking())
                .build();
        exp = workExperienceRepository.save(exp);
        return mapWorkExpToResponse(exp);
    }

    @Override
    @Transactional
    public WorkExperienceResponse updateWorkExperience(Long userId, Long expId, WorkExperienceRequest request) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + userId));
        WorkExperience exp = workExperienceRepository.findById(expId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkExperience", "id", expId));
        if (!exp.getCandidate().getId().equals(candidate.getId())) {
            throw new UnauthorizedException("You don't have permission to update this work experience");
        }
        exp.setCompany(request.getCompany());
        exp.setPosition(request.getPosition());
        exp.setStartDate(request.getStartDate());
        exp.setEndDate(request.getEndDate());
        exp.setDescription(request.getDescription());
        exp.setCurrentlyWorking(request.getCurrentlyWorking());
        exp = workExperienceRepository.save(exp);
        return mapWorkExpToResponse(exp);
    }

    @Override
    @Transactional
    public void deleteWorkExperience(Long userId, Long expId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + userId));
        WorkExperience exp = workExperienceRepository.findById(expId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkExperience", "id", expId));
        if (!exp.getCandidate().getId().equals(candidate.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this work experience");
        }
        workExperienceRepository.delete(exp);
    }

    @Override
    public List<WorkExperienceResponse> getWorkExperiences(Long userId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + userId));
        return workExperienceRepository.findByCandidateId(candidate.getId()).stream()
                .map(this::mapWorkExpToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addSkill(Long userId, Long skillId, String level) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + userId));
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", skillId));
        CandidateSkill cs = CandidateSkill.builder()
                .candidate(candidate)
                .skill(skill)
                .level(level)
                .build();
        candidateSkillRepository.save(cs);
    }

    @Override
    @Transactional
    public void removeSkill(Long userId, Long skillId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + userId));
        List<CandidateSkill> skills = candidateSkillRepository.findByCandidateId(candidate.getId());
        skills.stream()
                .filter(cs -> cs.getSkill().getId().equals(skillId))
                .findFirst()
                .ifPresent(candidateSkillRepository::delete);
    }

    private CandidateProfileResponse mapToResponse(Candidate candidate) {
        List<EducationResponse> educations = educationRepository.findByCandidateId(candidate.getId())
                .stream().map(this::mapEducationToResponse).collect(Collectors.toList());
        List<WorkExperienceResponse> workExps = workExperienceRepository.findByCandidateId(candidate.getId())
                .stream().map(this::mapWorkExpToResponse).collect(Collectors.toList());
        List<SkillResponse> skills = candidateSkillRepository.findByCandidateId(candidate.getId())
                .stream().map(cs -> SkillResponse.builder()
                        .id(cs.getSkill().getId())
                        .name(cs.getSkill().getName())
                        .level(cs.getLevel())
                        .build()).collect(Collectors.toList());

        return CandidateProfileResponse.builder()
                .id(candidate.getId())
                .fullName(candidate.getFullName())
                .phone(candidate.getPhone())
                .address(candidate.getAddress())
                .summary(candidate.getSummary())
                .cvUrl(candidate.getCvUrl())
                .avatarUrl(candidate.getAvatarUrl())
                .email(candidate.getUser().getEmail())
                .educations(educations)
                .workExperiences(workExps)
                .skills(skills)
                .build();
    }

    private EducationResponse mapEducationToResponse(Education e) {
        return EducationResponse.builder()
                .id(e.getId())
                .school(e.getSchool())
                .major(e.getMajor())
                .degree(e.getDegree())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .description(e.getDescription())
                .build();
    }

    private WorkExperienceResponse mapWorkExpToResponse(WorkExperience w) {
        return WorkExperienceResponse.builder()
                .id(w.getId())
                .company(w.getCompany())
                .position(w.getPosition())
                .startDate(w.getStartDate())
                .endDate(w.getEndDate())
                .description(w.getDescription())
                .currentlyWorking(w.getCurrentlyWorking())
                .build();
    }
}
