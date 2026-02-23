package com.freelance.collaborationservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCollaborationRequestDTO {

    @NotNull(message = "Collaboration ID is required")
    @Positive(message = "Collaboration ID must be positive")
    private Long collaborationId;

    @NotNull(message = "Freelancer ID is required")
    @Positive(message = "Freelancer ID must be positive")
    private Long freelancerId;

    private String proposalMessage;

    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal proposedPrice;
}
