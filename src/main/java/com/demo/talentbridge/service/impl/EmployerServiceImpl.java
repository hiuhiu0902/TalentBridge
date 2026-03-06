package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.request.EmployerProfileRequest;
import com.demo.talentbridge.dto.response.EmployerProfileResponse;
import com.demo.talentbridge.entity.Employer;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.repository.EmployerRepository;
import com.demo.talentbridge.repository.FollowConnectionRepository;
import com.demo.talentbridge.service.EmployerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployerServiceImpl implements EmployerService {

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private FollowConnectionRepository followConnectionRepository;

    @Override
    public EmployerProfileResponse getProfile(Long userId) {
        Employer employer = employerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found for user: " + userId));
        return mapToResponse(employer);
    }

    @Override
    @Transactional
    public EmployerProfileResponse updateProfile(Long userId, EmployerProfileRequest request) {
        Employer employer = employerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer profile not found for user: " + userId));
        employer.setCompanyName(request.getCompanyName());
        if (request.getWebsite() != null) employer.setWebsite(request.getWebsite());
        if (request.getDescription() != null) employer.setDescription(request.getDescription());
        if (request.getLogoUrl() != null) employer.setLogoUrl(request.getLogoUrl());
        if (request.getIndustry() != null) employer.setIndustry(request.getIndustry());
        if (request.getCompanySize() != null) employer.setCompanySize(request.getCompanySize());
        if (request.getAddress() != null) employer.setAddress(request.getAddress());
        employer = employerRepository.save(employer);
        return mapToResponse(employer);
    }

    @Override
    public EmployerProfileResponse getProfileByEmployerId(Long employerId) {
        Employer employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new ResourceNotFoundException("Employer", "id", employerId));
        return mapToResponse(employer);
    }

    private EmployerProfileResponse mapToResponse(Employer employer) {
        long followerCount = followConnectionRepository.countByFollowedId(employer.getUser().getId());
        return EmployerProfileResponse.builder()
                .id(employer.getId())
                .name(employer.getUser().getFullName())
                .companyName(employer.getCompanyName())
                .website(employer.getWebsite())
                .description(employer.getDescription())
                .logoUrl(employer.getLogoUrl())
                .industry(employer.getIndustry())
                .companySize(employer.getCompanySize())
                .address(employer.getAddress())
                .email(employer.getUser().getEmail())
                .followerCount(followerCount)
                .build();
    }
}
