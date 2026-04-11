package com.freelancify.badge.dto;

import com.freelancify.badge.entity.BadgeLevel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BadgeDTO {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;
    private BadgeLevel level;
    private int minMissionsCompleted;
    private double minAverageRating;
    private boolean active;
    private LocalDateTime earnedAt; // populated when returned as part of a freelancer profile
}
