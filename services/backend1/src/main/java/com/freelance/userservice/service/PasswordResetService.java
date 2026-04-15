package com.freelance.userservice.service;

import com.freelance.userservice.model.ResetPasswordToken;
import com.freelance.userservice.model.User;
import com.freelance.userservice.repository.ResetPasswordTokenRepository;
import com.freelance.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Service for password reset token management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    
    private final ResetPasswordTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * Generate a secure random token
     */
    private String generateToken() {
        byte[] randomBytes = new byte[48];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    /**
     * Create a password reset token for a user
     * @param email User email
     * @return Reset token string
     */
    @Transactional
    public String createResetToken(String email) {
        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.info("Password reset token requested for non-existent email: {}", email);
            // Still return a token to avoid revealing if user exists
            return generateToken();
        }
        
        User user = userOpt.get();
        
        // Invalidate any existing tokens for this email
        tokenRepository.findByEmail(email).ifPresent(tokenRepository::delete);
        
        // Create new token
        String token = generateToken();
        ResetPasswordToken resetToken = new ResetPasswordToken();
        resetToken.setToken(token);
        resetToken.setEmail(email);
        resetToken.setKeycloakUserId(user.getKeycloakId());
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1)); // 1 hour expiration
        resetToken.setUsed(false);
        
        tokenRepository.save(resetToken);
        log.info("Password reset token created for email: {}", email);
        
        return token;
    }
    
    /**
     * Validate and use a reset token
     * @param token Token string
     * @return ResetPasswordToken if valid, null otherwise
     */
    @Transactional
    public ResetPasswordToken validateAndUseToken(String token) {
        Optional<ResetPasswordToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            log.warn("Invalid reset token: {}", token);
            return null;
        }
        
        ResetPasswordToken resetToken = tokenOpt.get();
        
        if (!resetToken.isValid()) {
            log.warn("Reset token expired or already used: {}", token);
            return null;
        }
        
        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        log.info("Reset token validated and used for email: {}", resetToken.getEmail());
        return resetToken;
    }
    
    /**
     * Reset password using token
     * @param token Token string
     * @param newPassword New password
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        ResetPasswordToken resetToken = validateAndUseToken(token);
        
        if (resetToken == null) {
            return false;
        }
        
        try {
            // Reset password in Keycloak
            keycloakService.resetPassword(resetToken.getKeycloakUserId(), newPassword);
            log.info("Password reset successfully for email: {}", resetToken.getEmail());
            return true;
        } catch (Exception e) {
            log.error("Error resetting password for token: {}", token, e);
            return false;
        }
    }
    
    /**
     * Clean up expired tokens (can be called periodically)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("Cleaned up expired password reset tokens");
    }
}
