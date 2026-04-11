package com.freelancify.badge.dto;

import lombok.Data;

@Data
public class FreelancerStatsDTO {
    private String freelancerEmail;
    private String freelancerName;
    private int missionsCompleted;
    private double averageRating;
}
