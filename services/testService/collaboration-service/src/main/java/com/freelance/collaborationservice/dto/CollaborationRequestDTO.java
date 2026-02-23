package com.freelance.collaborationservice.dto;

import com.freelance.collaborationservice.model.CollaborationRequestStatus;
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
public class CollaborationRequestDTO {
    private Long id;
    private Long collaborationId;
    private Long freelancerId;
    private String proposalMessage;
    private BigDecimal proposedPrice;
    private CollaborationRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
