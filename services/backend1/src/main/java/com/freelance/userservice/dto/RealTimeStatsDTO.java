package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Real-time Statistics DTO
 * Widgets showing current statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeStatsDTO {
    
    /**
     * Total number of users in the system
     */
    private Long totalUsers;
    
    /**
     * New users registered this month
     */
    private Long newUsersThisMonth;
    
    /**
     * Active users today (users who logged in today)
     * Note: This is a placeholder - in a real system, you'd track login activity
     */
    private Long activeUsersToday;
    
    /**
     * Projects in progress
     * Note: This is a placeholder - projects would come from a Project Service
     */
    private Long projectsInProgress;
    
    /**
     * Total revenue
     * Note: This is a placeholder - revenue would come from a Payment/Transaction Service
     */
    private Double totalRevenue;
    
    /**
     * Percentage change from last month (for totalUsers)
     */
    private Double totalUsersChange;
    
    /**
     * Percentage change from last month (for newUsersThisMonth)
     */
    private Double newUsersChange;
    
    /**
     * Percentage change from last month (for activeUsersToday)
     */
    private Double activeUsersChange;
    
    /**
     * Percentage change from last month (for projectsInProgress)
     */
    private Double projectsChange;
    
    /**
     * Percentage change from last month (for totalRevenue)
     */
    private Double revenueChange;
}
