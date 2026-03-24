package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.request.*;
import com.demo.talentbridge.dto.response.AuthResponse;
import com.demo.talentbridge.dto.response.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getCurrentUser(String email);
    AuthResponse googleLogin(GoogleAuthRequest request);

    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
