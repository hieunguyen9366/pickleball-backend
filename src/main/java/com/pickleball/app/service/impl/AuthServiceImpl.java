package com.pickleball.app.service.impl;

import com.pickleball.app.dto.auth.AuthResponse;
import com.pickleball.app.dto.auth.LoginRequest;
import com.pickleball.app.dto.auth.RegisterRequest;
import com.pickleball.app.dto.auth.ResetPasswordRequest;
import com.pickleball.app.entity.EmailVerificationToken;
import com.pickleball.app.entity.PasswordResetToken;
import com.pickleball.app.entity.User;
import com.pickleball.app.enums.UserRole;
import com.pickleball.app.enums.UserStatus;
import com.pickleball.app.repository.EmailVerificationTokenRepository;
import com.pickleball.app.repository.PasswordResetTokenRepository;
import com.pickleball.app.repository.UserRepository;
import com.pickleball.app.security.UserDetailsServiceImpl;
import com.pickleball.app.service.AuthService;
import com.pickleball.app.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;
        private final AuthenticationManager authenticationManager;
        private final UserDetailsServiceImpl userDetailsService;
        private final com.pickleball.app.service.EmailService emailService;
        private final com.pickleball.app.service.NotificationService notificationService;
        private final PasswordResetTokenRepository passwordResetTokenRepository;
        private final EmailVerificationTokenRepository emailVerificationTokenRepository;

        @Value("${app.frontend.url:http://localhost:4200}")
        private String frontendUrl;

        @Override
        public AuthResponse register(RegisterRequest request) {
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("Email already exists");
                }

                var user = User.builder()
                                .fullName(request.getFullName())
                                .email(request.getEmail())
                                .passwordHash(passwordEncoder.encode(request.getPassword()))
                                .phoneNumber(request.getPhoneNumber())
                                .role(UserRole.CUSTOMER) // Default role
                                .status(UserStatus.ACTIVE)
                                .emailVerified(false) // Email chưa được xác thực
                                .build();

                User savedUser = userRepository.save(user);

                // Tạo email verification token
                String verificationToken = UUID.randomUUID().toString();
                EmailVerificationToken emailToken = EmailVerificationToken.builder()
                                .token(verificationToken)
                                .user(savedUser)
                                .expiresAt(LocalDateTime.now().plusHours(24)) // 24 hours
                                .verified(false)
                                .build();
                emailVerificationTokenRepository.save(emailToken);

                // Notification & Email
                notificationService.createNotification(savedUser.getUserId(), "Chào mừng!",
                                "Chào mừng bạn đến với hệ thống đặt sân Pickleball.", "SYSTEM");
                try {
                        String verificationLink = frontendUrl + "/player/verify-email?token=" + verificationToken;
                        String emailContent = String.format(
                                        "Cảm ơn bạn đã đăng ký tài khoản!\n\n" +
                                                        "Vui lòng xác thực email của bạn bằng cách click vào link sau:\n" +
                                                        "%s\n\n" +
                                                        "Link này sẽ hết hạn sau 24 giờ.\n\n" +
                                                        "Chúc bạn có những giây phút thể thao vui vẻ!",
                                        verificationLink);
                        emailService.sendHtmlMessage(savedUser.getEmail(), "Xác thực email - Pickleball",
                                        emailContent.replace("\n", "<br>"));
                } catch (Exception e) {
                        log.error("Failed to send verification email", e);
                }

                // Auto login after register
                UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
                var jwtToken = jwtUtil.generateToken(userDetails);

                return AuthResponse.builder()
                                .token(jwtToken)
                                .refreshToken(jwtUtil.generateRefreshToken(userDetails))
                                .type("Bearer")
                                .id(savedUser.getUserId())
                                .email(savedUser.getEmail())
                                .role(savedUser.getRole().name())
                                .fullName(savedUser.getFullName())
                                .phoneNumber(savedUser.getPhoneNumber())
                                .build();
        }

        @Override
        public AuthResponse login(LoginRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.getEmail(),
                                                request.getPassword()));

                var user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                var jwtToken = jwtUtil.generateToken(userDetails);

                // Fixed: refreshToken not generated in original code snippet
                String refreshToken = jwtUtil.generateRefreshToken(userDetails);

                return AuthResponse.builder()
                                .token(jwtToken)
                                .refreshToken(refreshToken)
                                .type("Bearer")
                                .id(user.getUserId())
                                .email(user.getEmail())
                                .role(user.getRole().name())
                                .fullName(user.getFullName())
                                .phoneNumber(user.getPhoneNumber())
                                .build();
        }

        @Override
        public AuthResponse refreshToken(String token) {
                String userEmail = jwtUtil.extractUsername(token);
                if (userEmail != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                        if (jwtUtil.isTokenValid(token, userDetails)) {
                                var newToken = jwtUtil.generateToken(userDetails);
                                var user = userRepository.findByEmail(userEmail).orElseThrow();
                                return AuthResponse.builder()
                                                .token(newToken)
                                                .refreshToken(token) // Reuse old refresh token
                                                .type("Bearer")
                                                .id(user.getUserId())
                                                .email(user.getEmail())
                                                .role(user.getRole().name())
                                                .fullName(user.getFullName())
                                                .phoneNumber(user.getPhoneNumber())
                                                .build();
                        }
                }
                throw new RuntimeException("Invalid refresh token");
        }

        @Override
        @Transactional
        public void forgotPassword(String email) {
                User user = userRepository.findByEmail(email).orElse(null);
                if (user == null) {
                        // Không báo lỗi để tránh email enumeration attack
                        log.info("Password reset requested for non-existent email: {}", email);
                        return;
                }

                // Xóa các token cũ của user này
                passwordResetTokenRepository.deleteByUser(user);

                // Tạo token mới
                String token = UUID.randomUUID().toString();
                PasswordResetToken resetToken = PasswordResetToken.builder()
                                .token(token)
                                .user(user)
                                .expiresAt(LocalDateTime.now().plusHours(1)) // 1 hour expiry
                                .used(false)
                                .build();
                passwordResetTokenRepository.save(resetToken);

                // Gửi email với link reset
                try {
                        String resetLink = frontendUrl + "/player/reset-password?token=" + token;
                        String emailContent = String.format(
                                        "Bạn đã yêu cầu khôi phục mật khẩu.\n\n" +
                                                        "Click vào link sau để đặt lại mật khẩu:\n" +
                                                        "%s\n\n" +
                                                        "Link này sẽ hết hạn sau 1 giờ.\n\n" +
                                                        "Nếu bạn không yêu cầu khôi phục mật khẩu, vui lòng bỏ qua email này.",
                                        resetLink);
                        emailService.sendHtmlMessage(email, "Khôi phục mật khẩu - Pickleball",
                                        emailContent.replace("\n", "<br>"));
                        log.info("Password reset email sent to: {}", email);
                } catch (Exception e) {
                        log.error("Failed to send password reset email to {}", email, e);
                }
        }

        @Override
        @Transactional
        public void resetPassword(ResetPasswordRequest request) {
                PasswordResetToken token = passwordResetTokenRepository
                                .findByTokenAndUsedFalseAndExpiresAtAfter(request.getToken(), LocalDateTime.now())
                                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

                User user = token.getUser();
                user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(user);

                // Đánh dấu token đã sử dụng
                token.setUsed(true);
                passwordResetTokenRepository.save(token);

                log.info("Password reset successful for user: {}", user.getEmail());
        }

        @Override
        @Transactional
        public void verifyEmail(String token) {
                EmailVerificationToken emailToken = emailVerificationTokenRepository
                                .findByTokenAndVerifiedFalseAndExpiresAtAfter(token, LocalDateTime.now())
                                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token"));

                User user = emailToken.getUser();
                user.setEmailVerified(true);
                userRepository.save(user);

                // Đánh dấu token đã verified
                emailToken.setVerified(true);
                emailVerificationTokenRepository.save(emailToken);

                log.info("Email verified for user: {}", user.getEmail());
        }
}
