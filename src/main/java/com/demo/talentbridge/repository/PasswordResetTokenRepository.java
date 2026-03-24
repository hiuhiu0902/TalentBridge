package com.demo.talentbridge.repository;

import com.demo.talentbridge.entity.PasswordResetToken;
import com.demo.talentbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHashAndUsedFalse(String tokenHash);
    List<PasswordResetToken> findAllByUserAndUsedFalse(User user);
}