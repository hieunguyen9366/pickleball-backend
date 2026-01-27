package com.pickleball.app.repository;

import com.pickleball.app.entity.PasswordResetToken;
import com.pickleball.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByTokenAndUsedFalseAndExpiresAtAfter(String token, LocalDateTime now);
    void deleteByUser(User user);
    void deleteByExpiresAtBefore(LocalDateTime now);
}

