package com.pickleball.app.repository;

import com.pickleball.app.entity.EmailVerificationToken;
import com.pickleball.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    Optional<EmailVerificationToken> findByTokenAndVerifiedFalseAndExpiresAtAfter(String token, LocalDateTime now);
    Optional<EmailVerificationToken> findByUserAndVerifiedFalse(User user);
    void deleteByUser(User user);
    void deleteByExpiresAtBefore(LocalDateTime now);
}

