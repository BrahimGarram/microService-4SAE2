package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role Distribution DTO
 * For pie chart showing user distribution by role
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDistributionDTO {
    
    /**
     * User role (USER, FREELANCER, ADMIN)
     */
    private String role;
    
    /**
     * Number of users with this role
     */
    private Long count;
    
    /**
     * Percentage of total users
     */
    private Double percentage;
}
