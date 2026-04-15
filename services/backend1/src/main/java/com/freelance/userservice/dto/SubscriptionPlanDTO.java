package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanDTO {
    private Long id;
    private String planName;
    private String description;
    private BigDecimal price;
    private String billingCycle;
    private Integer maxBidsPerMonth;
    private BigDecimal commissionRate;
    private Boolean hasAnalyticsAccess;
    private Boolean hasProfileBoost;
    private Boolean hasPrioritySupport;
    private Boolean hasFeaturedListing;
    private Integer maxProjects;
    private Boolean isActive;
}
