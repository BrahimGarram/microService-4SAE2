package com.freelancify.badge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "badges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(length = 100)
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeLevel level; // BRONZE, SILVER, GOLD, PLATINUM

    // Criteria fields
    @Column(nullable = false)
    private int minMissionsCompleted; // minimum completed contracts/missions

    @Column(nullable = false)
    private double minAverageRating;  // minimum average rating (0-5)

    @Column(nullable = false)
    private boolean active = true;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
