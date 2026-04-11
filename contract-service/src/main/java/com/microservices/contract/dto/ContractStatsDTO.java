package com.microservices.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractStatsDTO {
    private long totalContracts;
    private Map<String, Long> countByStatus;
    private Map<String, BigDecimal> revenueByFreelancer;
    private long expiringIn7Days;
    private long overdueContracts;
}
