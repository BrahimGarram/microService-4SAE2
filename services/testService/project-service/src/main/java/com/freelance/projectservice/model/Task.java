package com.freelance.projectservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Task entity - represents a task within a project
 */
@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(length = 2000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.TO_DO;
    
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    
    @Column(name = "assigned_to")
    private Long assignedTo; // User ID (freelancer)
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy; // User ID (project owner or admin)
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "priority")
    private Integer priority = 0; // 0 = Low, 1 = Medium, 2 = High
    
    @Column(name = "order_index")
    private Integer orderIndex = 0; // For sorting tasks
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == TaskStatus.DONE && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
}
