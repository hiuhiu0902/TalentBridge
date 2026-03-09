package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.request.GoogleAuthRequest;
import com.demo.talentbridge.dto.request.LoginRequest;
import com.demo.talentbridge.dto.request.RegisterRequest;
import com.demo.talentbridge.dto.response.AuthResponse;
import com.demo.talentbridge.dto.response.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getCurrentUser(String email);
    AuthResponse googleLogin(GoogleAuthRequest request);
}
