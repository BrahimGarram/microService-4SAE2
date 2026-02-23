package com.freelance.collaborationservice.dto;

import com.freelance.collaborationservice.model.CollaborationType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCollaborationRequest {

    @NotNull(message = "Company ID is required")
    @Positive(message = "Company ID must be positive")
    private Long companyId;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 300)
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Collaboration type is required")
    private CollaborationType collaborationType;

    @NotBlank(message = "Required skills are required")
    private String requiredSkills;

    @NotNull(message = "Minimum budget is required")
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal budgetMin;

    @NotNull(message = "Maximum budget is required")
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal budgetMax;

    @NotBlank(message = "Estimated duration is required")
    @Size(max = 100)
    private String estimatedDuration;

    @NotBlank(message = "Complexity level is required")
    @Size(max = 50)
    private String complexityLevel;

    @NotNull(message = "Deadline is required")
    private LocalDateTime deadline;

    @NotNull(message = "Confidentiality option (NDA) is required")
    private Boolean confidentialityOption;

    private Integer maxFreelancersNeeded;
    private String milestoneStructure;
    private String attachments;
    @Size(max = 100)
    private String industry;
}
