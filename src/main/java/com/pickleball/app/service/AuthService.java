package com.pickleball.app.service;

import com.pickleball.app.dto.auth.AuthResponse;
import com.pickleball.app.dto.auth.LoginRequest;
import com.pickleball.app.dto.auth.RegisterRequest;
import com.pickleball.app.dto.auth.ResetPasswordRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String token);

    void forgotPassword(String email);

    void resetPassword(ResetPasswordRequest request);

    void verifyEmail(String token);
}
