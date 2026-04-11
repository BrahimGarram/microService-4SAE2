package com.freelance.projectservice.dto;

import com.freelance.projectservice.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Task entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Long projectId;
    private Long assignedTo;
    private String assignedToName; // Freelancer name (from user-service)
    private Long createdBy;
    private String createdByName; // Creator name (from user-service)
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private Integer priority;
    private Integer orderIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
