package com.pickleball.app.controller;

import com.pickleball.app.dto.auth.AuthResponse;
import com.pickleball.app.dto.auth.LoginRequest;
import com.pickleball.app.dto.auth.RegisterRequest;
import com.pickleball.app.dto.common.ApiResponse;
import com.pickleball.app.dto.common.UserDTO;
import com.pickleball.app.entity.User;
import com.pickleball.app.repository.UserRepository;
import com.pickleball.app.service.AuthService;
import com.pickleball.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService; // For profile update
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.register(request)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody Map<String, String> body) {
        String token = body.get("refreshToken");
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(token)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> forgotPassword(@RequestBody com.pickleball.app.dto.auth.ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        Map<String, String> response = new java.util.HashMap<>();
        // Luôn trả về message giống nhau để tránh email enumeration
        response.put("message", "Nếu email tồn tại, chúng tôi đã gửi link khôi phục mật khẩu đến email của bạn.");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetPassword(@Valid @RequestBody com.pickleball.app.dto.auth.ResetPasswordRequest request) {
        authService.resetPassword(request);
        Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Mật khẩu đã được đặt lại thành công. Vui lòng đăng nhập lại.");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Email đã được xác thực thành công.");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody UserDTO dto) {
        // Frontend expects { message: string; user: User }
        UserDTO updated = userService.updateProfile(getCurrentUser(), dto);
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("message", "Cập nhật thông tin thành công");
        response.put("user", updated);
        return ResponseEntity.ok(response);
    }
}
