package com.freelance.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Reset Password Token Entity
 * 
 * Stores password reset tokens for users.
 * Tokens expire after 1 hour.
 */
@Entity
@Table(name = "reset_password_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Token string (unique, randomly generated)
     */
    @Column(unique = true, nullable = false, length = 64)
    private String token;
    
    /**
     * User email associated with this token
     */
    @Column(nullable = false)
    private String email;
    
    /**
     * Keycloak user ID
     */
    @Column(nullable = false)
    private String keycloakUserId;
    
    /**
     * Token expiration time (1 hour from creation)
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    /**
     * Whether this token has been used
     */
    @Column(nullable = false)
    private Boolean used = false;
    
    /**
     * When the token was created
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusHours(1); // Token expires in 1 hour
        }
    }
    
    /**
     * Check if token is valid (not expired and not used)
     */
    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }
}
