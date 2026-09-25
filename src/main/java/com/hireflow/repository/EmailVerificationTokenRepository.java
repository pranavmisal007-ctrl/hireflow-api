package com.hireflow.repository;

import com.hireflow.entity.EmailVerificationToken;
import com.hireflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByTokenAndUser(String token, User user);
    Optional<EmailVerificationToken> findTopByUserOrderByExpiryDateDesc(User user);
    void deleteByUser(User user);
}
