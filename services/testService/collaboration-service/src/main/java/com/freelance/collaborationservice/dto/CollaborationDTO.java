package com.freelance.collaborationservice.dto;

import com.freelance.collaborationservice.model.CollaborationStatus;
import com.freelance.collaborationservice.model.CollaborationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollaborationDTO {
    private Long id;
    private Long companyId;
    private String title;
    private String description;
    private CollaborationType collaborationType;
    private String requiredSkills;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String estimatedDuration;
    private String complexityLevel;
    private LocalDateTime deadline;
    private Boolean confidentialityOption;
    private Integer maxFreelancersNeeded;
    private String milestoneStructure;
    private String attachments;
    private String industry;
    private CollaborationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
