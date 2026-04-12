package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Performance Indicators DTO
 * Key performance metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceIndicatorsDTO {
    
    /**
     * Conversion rate (percentage)
     * Note: This is a placeholder - would require tracking signup to active user conversion
     */
    private Double conversionRate;
    
    /**
     * Average session time (in minutes)
     * Note: This is a placeholder - would require session tracking
     */
    private Double averageSessionTime;
    
    /**
     * Retention rate (percentage)
     * Note: This is a placeholder - would require tracking user return rate
     */
    private Double retentionRate;
    
    /**
     * Completed projects vs cancelled projects ratio
     * Note: This is a placeholder - would come from Project Service
     */
    private Long completedProjects;
    
    /**
     * Cancelled projects count
     * Note: This is a placeholder - would come from Project Service
     */
    private Long cancelledProjects;
    
    /**
     * Completion rate (percentage)
     */
    private Double completionRate;
}
