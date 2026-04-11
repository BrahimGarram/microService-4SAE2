package com.freelancify.badge.dto;

import com.freelancify.badge.entity.BadgeLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntryDTO {
    private int rank;
    private String freelancerEmail;
    private String freelancerName;
    private int missionsCompleted;
    private double averageRating;
    private BadgeLevel currentLevel;
    private String currentBadgeName;
    private List<BadgeDTO> badges;

    // XP system
    private int totalXp;
    private int xpForCurrentLevel; // XP threshold of current level
    private int xpForNextLevel;    // XP threshold of next level
    private int xpProgress;        // XP earned since current level start
    private int xpNeeded;          // XP still needed to reach next level
    private int progressPercent;   // 0-100 for the progress bar
    private String nextLevelName;  // label of the next level
}
