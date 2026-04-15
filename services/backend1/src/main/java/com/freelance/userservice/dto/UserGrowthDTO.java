package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Growth DTO
 * For line chart showing user growth over time
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGrowthDTO {
    
    /**
     * Period label (e.g., "Jan", "Feb", "2024-01", etc.)
     */
    private String period;
    
    /**
     * Number of users at this period
     */
    private Long userCount;
    
    /**
     * Number of new users in this period
     */
    private Long newUsers;
}
