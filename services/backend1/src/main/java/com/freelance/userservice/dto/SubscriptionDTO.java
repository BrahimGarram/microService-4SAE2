package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDTO {
    private Long id;
    private Long userId;
    private SubscriptionPlanDTO plan;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private String paymentTransactionId;
    private BigDecimal amountPaid;
    private Integer bidsUsedThisMonth;
    private LocalDateTime lastBidResetDate;
    private Boolean autoRenew;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
