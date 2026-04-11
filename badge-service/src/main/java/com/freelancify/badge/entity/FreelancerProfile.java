package com.freelancify.badge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Stores gamification stats for each freelancer.
 * Updated manually by admin or via the /update-stats endpoint.
 */
@Entity
@Table(name = "freelancer_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FreelancerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String freelancerEmail;

    @Column(nullable = false, length = 100)
    private String freelancerName;

    @Column(nullable = false)
    private int missionsCompleted = 0;

    @Column(nullable = false)
    private double averageRating = 0.0;

    @Enumerated(EnumType.STRING)
    private BadgeLevel currentLevel; // computed automatically

    private String currentBadgeName; // name of the highest earned badge

    @Column(nullable = false)
    private int rankPosition = 0;

    @Column(nullable = false)
    private int totalXp = 0;

    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
