package com.freelance.projectservice.dto;

import com.freelance.projectservice.model.ProposalStatus;
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
public class ProposalDTO {

    private Long id;
    private Long projectId;
    private Long freelancerId;
    private BigDecimal amount;
    private LocalDateTime proposedDeadline;
    private String message;
    private ProposalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
