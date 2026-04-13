package com.freelance.projectservice.dto;

import com.freelance.projectservice.model.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Project Data Transfer Object
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDTO {
    
    private Long id;
    private String title;
    private String description;
    private ProjectStatus status;
    private Long ownerId;
    private BigDecimal budget;
    private LocalDateTime deadline;
    private String category;
    private String imageUrl;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
