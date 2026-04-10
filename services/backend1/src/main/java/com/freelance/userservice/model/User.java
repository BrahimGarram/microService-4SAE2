package com.freelance.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * User Entity
 * 
 * Stores user profile information in MySQL database.
 * Note: Password is NOT stored here - Keycloak handles authentication.
 * 
 * The keycloakId field stores the 'sub' claim from JWT token,
 * which uniquely identifies the user in Keycloak.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Keycloak user ID (sub claim from JWT)
     * This links the database user to Keycloak user
     */
    @Column(unique = true, nullable = false)
    private String keycloakId;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    /**
     * User role: USER, FREELANCER, ADMIN, or ENTERPRISE
     * This should match the roles defined in Keycloak realm
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;
    
    /**
     * Timestamp when user was created in database
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when user was last updated
     */
    private LocalDateTime updatedAt;
    
    /**
     * User's country (detected from IP address during registration)
     */
    @Column(length = 100)
    private String country;
    
    /**
     * Profile picture URL or path
     */
    @Column(length = 500)
    private String profilePicture;
    
    /**
     * Bio/description personnelle
     */
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    /**
     * City/Location
     */
    @Column(length = 100)
    private String city;
    
    /**
     * Timezone (e.g., "America/New_York", "Europe/Paris")
     */
    @Column(length = 50)
    private String timezone;
    
    /**
     * Hourly rate (for freelancers)
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal hourlyRate;
    
    /**
     * Availability status
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserAvailability availability = UserAvailability.OFFLINE;
    
    /**
     * Verified status (badge vérifié)
     */
    @Column(nullable = false)
    private Boolean verified = false;

    /**
     * Token balance (synced from payment-service wallet). Used for subscriptions, etc.
     */
    @Column(name = "token_balance", nullable = false)
    private Integer tokenBalance = 0;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (availability == null) {
            availability = UserAvailability.OFFLINE;
        }
        if (verified == null) {
            verified = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
