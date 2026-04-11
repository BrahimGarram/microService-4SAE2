package com.microservices.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatsDTO {
    private long totalPayments;
    private BigDecimal totalRevenue;
    private BigDecimal averagePaymentAmount;
    private Map<String, Long> countByStatus;
    private Map<String, BigDecimal> revenueByMethod;
    private Map<String, BigDecimal> revenueByMonth;
    private long failedOlderThan7Days;
    private long duplicateTransactions;
}
