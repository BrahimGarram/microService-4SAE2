package com.microservices.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractSummaryDTO {
    private Integer idContract;
    private String contractTitle;
    private String freelancerName;
    private String clientName;
    private String status;
    private BigDecimal contractAmount;
    private String currency;
    private long daysRemaining;
    private boolean isExpiringSoon;
    private boolean isOverdue;
}
