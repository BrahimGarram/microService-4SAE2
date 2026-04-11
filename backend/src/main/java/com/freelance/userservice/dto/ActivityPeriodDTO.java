package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Activity Period DTO
 * For bar chart showing activity by day/week/month
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityPeriodDTO {
    
    /**
     * Period label (e.g., "Mon", "Tue", "Week 1", "Jan", etc.)
     */
    private String period;
    
    /**
     * Number of new registrations in this period
     */
    private Long registrations;
    
    /**
     * Number of active users in this period
     * Note: This is a placeholder - would require activity tracking
     */
    private Long activeUsers;
}
