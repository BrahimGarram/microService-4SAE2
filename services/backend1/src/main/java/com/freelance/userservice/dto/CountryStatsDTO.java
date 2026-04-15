package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Country Statistics DTO
 * For geographic distribution of users
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryStatsDTO {
    
    /**
     * Country name or code
     */
    private String country;
    
    /**
     * Number of users from this country
     */
    private Long userCount;
    
    /**
     * Percentage of total users
     */
    private Double percentage;
}
