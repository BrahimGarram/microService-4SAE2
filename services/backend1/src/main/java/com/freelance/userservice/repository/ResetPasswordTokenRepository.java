package com.freelance.userservice.repository;

import com.freelance.userservice.model.ResetPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for ResetPasswordToken entity
 */
@Repository
public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, Long> {
    
    /**
     * Find token by token string
     */
    Optional<ResetPasswordToken> findByToken(String token);
    
    /**
     * Find token by email
     */
    Optional<ResetPasswordToken> findByEmail(String email);
    
    /**
     * Delete expired tokens
     */
    void deleteByExpiresAtBefore(java.time.LocalDateTime now);
}
