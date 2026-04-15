package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Dashboard Statistics DTO
 * Contains all statistics for the admin dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    
    /**
     * Real-time statistics widgets
     */
    private RealTimeStatsDTO realTimeStats;
    
    /**
     * User growth over time (for line chart)
     */
    private List<UserGrowthDTO> userGrowth;
    
    /**
     * Role distribution (for pie chart)
     */
    private List<RoleDistributionDTO> roleDistribution;
    
    /**
     * Activity by period (for bar chart)
     */
    private List<ActivityPeriodDTO> activityByPeriod;
    
    /**
     * Performance indicators
     */
    private PerformanceIndicatorsDTO performanceIndicators;
    
    /**
     * Geographic statistics
     */
    private List<CountryStatsDTO> topCountries;
}
