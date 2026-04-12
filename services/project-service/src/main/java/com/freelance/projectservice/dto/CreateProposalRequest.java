package com.freelance.projectservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProposalRequest {

    @NotNull
    @Positive
    private Long projectId;

    @NotNull
    @Positive
    private Long freelancerId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal amount;

    private LocalDateTime proposedDeadline;

    @Size(max = 2000)
    private String message;
}
