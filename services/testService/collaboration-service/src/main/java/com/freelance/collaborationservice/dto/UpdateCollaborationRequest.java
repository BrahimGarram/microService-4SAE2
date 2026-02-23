package com.freelance.collaborationservice.dto;

import com.freelance.collaborationservice.model.CollaborationStatus;
import com.freelance.collaborationservice.model.CollaborationType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCollaborationRequest {

    @Size(min = 3, max = 300)
    private String title;

    private String description;

    private CollaborationType collaborationType;

    private String requiredSkills;

    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal budgetMin;

    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal budgetMax;

    @Size(max = 100)
    private String estimatedDuration;

    @Size(max = 50)
    private String complexityLevel;

    private LocalDateTime deadline;

    private Boolean confidentialityOption;

    private Integer maxFreelancersNeeded;
    private String milestoneStructure;
    private String attachments;

    @Size(max = 100)
    private String industry;

    private CollaborationStatus status;
}
