package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.request.EmployerProfileRequest;
import com.demo.talentbridge.dto.response.EmployerProfileResponse;

public interface EmployerService {
    EmployerProfileResponse getProfile(Long userId);
    EmployerProfileResponse updateProfile(Long userId, EmployerProfileRequest request);
    EmployerProfileResponse getProfileByEmployerId(Long employerId);
    EmployerProfileResponse createProfile(Long userId, EmployerProfileRequest request);
}