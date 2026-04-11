package com.freelance.projectservice.dto;

import com.freelance.projectservice.model.TaskStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for creating a task
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    @NotNull(message = "Project ID is required")
    @Positive(message = "Project ID must be positive")
    private Long projectId;
    
    private Long assignedTo; // Optional - freelancer user ID
    
    @NotNull(message = "Created by user ID is required")
    @Positive(message = "Created by user ID must be positive")
    private Long createdBy;
    
    private LocalDateTime dueDate;
    
    @Min(value = 0, message = "Priority must be 0 (Low), 1 (Medium), or 2 (High)")
    @Max(value = 2, message = "Priority must be 0 (Low), 1 (Medium), or 2 (High)")
    private Integer priority = 0;
    
    private Integer orderIndex = 0;
}
