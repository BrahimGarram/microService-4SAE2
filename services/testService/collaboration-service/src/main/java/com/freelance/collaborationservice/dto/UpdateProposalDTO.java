package com.freelance.collaborationservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProposalDTO {
    private String proposalMessage;
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal proposedPrice;
}
