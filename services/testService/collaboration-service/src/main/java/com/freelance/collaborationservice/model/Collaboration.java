package com.freelance.collaborationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "collaborations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Collaboration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CollaborationType collaborationType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String requiredSkills;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal budgetMin;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal budgetMax;

    @Column(nullable = false, length = 100)
    private String estimatedDuration;

    @Column(nullable = false, length = 50)
    private String complexityLevel;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(nullable = false)
    private Boolean confidentialityOption = false;

    private Integer maxFreelancersNeeded;

    @Column(columnDefinition = "TEXT")
    private String milestoneStructure;

    @Column(columnDefinition = "TEXT")
    private String attachments;

    @Column(length = 100)
    private String industry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CollaborationStatus status = CollaborationStatus.OPEN;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = CollaborationStatus.OPEN;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
