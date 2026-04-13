package com.freelance.projectservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Statistics for the projects backoffice dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatsDTO {
    private long total;
    private Map<String, Long> byStatus;
    private Map<String, Long> byCategory;
    private BigDecimal totalBudget;
    private double averageBudget;
    private double completionRate;
}
