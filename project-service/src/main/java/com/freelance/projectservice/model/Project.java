package com.freelance.projectservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Project Entity
 * 
 * Stores project information in MySQL database.
 */
@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * Project status: DRAFT, OPEN, IN_PROGRESS, COMPLETED, CANCELLED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.DRAFT;
    
    /**
     * Owner ID - references user ID from user-service
     */
    @Column(nullable = false)
    private Long ownerId;
    
    /**
     * Budget for the project
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal budget;
    
    /**
     * Project deadline
     */
    private LocalDateTime deadline;
    
    /**
     * Project category (e.g., Web Development, Design, Mobile, etc.)
     */
    @Column(length = 100)
    private String category;
    
    /**
     * Project cover image URL
     */
    @Column(length = 500)
    private String imageUrl;
    
    /**
     * Project tags (comma-separated)
     */
    @Column(length = 500)
    private String tags;
    
    /**
     * Timestamp when project was created
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when project was last updated
     */
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ProjectStatus.DRAFT;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
