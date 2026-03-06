package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.request.*;
import com.demo.talentbridge.dto.response.CandidateProfileResponse;
import com.demo.talentbridge.dto.response.EducationResponse;
import com.demo.talentbridge.dto.response.SkillResponse;
import com.demo.talentbridge.dto.response.WorkExperienceResponse;
import com.demo.talentbridge.entity.*;
import com.demo.talentbridge.enums.SkillName;
import com.demo.talentbridge.exception.BadRequestException;
import com.demo.talentbridge.exception.DuplicateResourceException;
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
    private UserRepository userRepository;

    // ─── Profile ────────────────────────────────────────────────────────────────

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
        if (request.getFullName() != null) candidate.getUser().setFullName(request.getFullName());
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
    public CandidateProfileResponse createProfile(Long userId, CandidateProfileRequest request) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        if (request.getPhone() != null) candidate.setPhone(request.getPhone());
        if (request.getAddress() != null) candidate.setAddress(request.getAddress());
        if (request.getSummary() != null) candidate.setSummary(request.getSummary());
        if (request.getCvUrl() != null) candidate.setCvUrl(request.getCvUrl());
        if (request.getAvatarUrl() != null) candidate.setAvatarUrl(request.getAvatarUrl());

        candidate = candidateRepository.save(candidate);

        return mapToResponse(candidate);
    }


    // ─── Education ──────────────────────────────────────────────────────────────

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

    // ─── Work Experience ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public WorkExperienceResponse addWorkExperience(Long userId, WorkExperienceRequest request) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + userId));
        if (Boolean.TRUE.equals(request.getCurrentlyWorking()) && request.getEndDate() != null) {
            throw new BadRequestException("End date must be null when currently working");
        }
        WorkExperience exp = WorkExperience.builder()
                .candidate(candidate)
                .company(request.getCompany())
                .position(request.getPosition())
                .startDate(request.getStartDate())
                .endDate(Boolean.TRUE.equals(request.getCurrentlyWorking()) ? null : request.getEndDate())
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
        if (Boolean.TRUE.equals(request.getCurrentlyWorking()) && request.getEndDate() != null) {
            throw new BadRequestException("End date must be null when currently working");
        }
        exp.setCompany(request.getCompany());
        exp.setPosition(request.getPosition());
        exp.setStartDate(request.getStartDate());
        exp.setEndDate(Boolean.TRUE.equals(request.getCurrentlyWorking()) ? null : request.getEndDate());
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

    // ─── Skills ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SkillResponse addSkill(Long userId, CandidateSkillRequest request) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + userId));

        if (candidateSkillRepository.existsByCandidateIdAndSkillName(candidate.getId(), request.getSkillName())) {
            throw new DuplicateResourceException("Skill already added: " + request.getSkillName());
        }

        CandidateSkill cs = CandidateSkill.builder()
                .candidate(candidate)
                .skillName(request.getSkillName())
                .level(request.getLevel())
                .build();
        cs = candidateSkillRepository.save(cs);
        return mapSkillToResponse(cs);
    }

    @Override
    @Transactional
    public SkillResponse updateSkill(Long userId, CandidateSkillRequest request) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + userId));

        CandidateSkill cs = candidateSkillRepository
                .findByCandidateIdAndSkillName(candidate.getId(), request.getSkillName())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + request.getSkillName()));

        cs.setLevel(request.getLevel());
        cs = candidateSkillRepository.save(cs);
        return mapSkillToResponse(cs);
    }

    @Override
    @Transactional
    public void removeSkill(Long userId, String skillNameStr) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + userId));

        SkillName skillName;
        try {
            skillName = SkillName.valueOf(skillNameStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid skill name: " + skillNameStr);
        }

        CandidateSkill cs = candidateSkillRepository
                .findByCandidateIdAndSkillName(candidate.getId(), skillName)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + skillNameStr));

        candidateSkillRepository.delete(cs);
    }

    @Override
    public List<SkillResponse> getSkills(Long userId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found for user: " + userId));
        return candidateSkillRepository.findByCandidateId(candidate.getId()).stream()
                .map(this::mapSkillToResponse)
                .collect(Collectors.toList());
    }

    // ─── Mappers ────────────────────────────────────────────────────────────────

    private CandidateProfileResponse mapToResponse(Candidate candidate) {
        List<EducationResponse> educations = educationRepository.findByCandidateId(candidate.getId())
                .stream().map(this::mapEducationToResponse).collect(Collectors.toList());
        List<WorkExperienceResponse> workExps = workExperienceRepository.findByCandidateId(candidate.getId())
                .stream().map(this::mapWorkExpToResponse).collect(Collectors.toList());
        List<SkillResponse> skills = candidateSkillRepository.findByCandidateId(candidate.getId())
                .stream().map(this::mapSkillToResponse).collect(Collectors.toList());

        return CandidateProfileResponse.builder()
                .id(candidate.getId())
                .fullName(candidate.getUser().getFullName())
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

    private SkillResponse mapSkillToResponse(CandidateSkill cs) {
        return SkillResponse.builder()
                .skillName(cs.getSkillName())
                .displayName(cs.getSkillName() != null ? cs.getSkillName().name() : null)
                .level(cs.getLevel())
                .build();
    }
}
