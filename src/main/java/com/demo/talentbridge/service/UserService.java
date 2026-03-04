package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.request.UpdatePasswordRequest;
import com.demo.talentbridge.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    void updatePassword(Long userId, UpdatePasswordRequest request);
    void deactivateAccount(Long userId);
    void activateAccount(Long userId);
    UserResponse updateUserStatus(Long userId, boolean active);
}
