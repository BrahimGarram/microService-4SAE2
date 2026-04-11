package com.freelancify.badge.service;

import com.freelancify.badge.dto.BadgeDTO;
import com.freelancify.badge.dto.FreelancerStatsDTO;
import com.freelancify.badge.dto.LeaderboardEntryDTO;
import com.freelancify.badge.entity.*;
import com.freelancify.badge.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepo;
    private final FreelancerProfileRepository profileRepo;
    private final FreelancerBadgeRepository freelancerBadgeRepo;

    // XP thresholds per level
    private static final int XP_BRONZE   = 50;
    private static final int XP_SILVER   = 150;
    private static final int XP_GOLD     = 300;
    private static final int XP_PLATINUM = 500;

    // XP formula
    private int computeXp(int missions, double rating) {
        return (missions * 10) + (int)(rating * 5);
    }

    private BadgeLevel levelFromXp(int xp) {
        if (xp >= XP_PLATINUM) return BadgeLevel.PLATINUM;
        if (xp >= XP_GOLD)     return BadgeLevel.GOLD;
        if (xp >= XP_SILVER)   return BadgeLevel.SILVER;
        if (xp >= XP_BRONZE)   return BadgeLevel.BRONZE;
        return null;
    }

    // ── Badge CRUD ────────────────────────────────────────────────────────────

    public List<Badge> getAllBadges() {
        return badgeRepo.findAll();
    }

    public Badge createBadge(BadgeDTO dto) {
        Badge badge = new Badge();
        mapDtoToEntity(dto, badge);
        return badgeRepo.save(badge);
    }

    public Badge updateBadge(Long id, BadgeDTO dto) {
        Badge badge = badgeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Badge not found: " + id));
        mapDtoToEntity(dto, badge);
        return badgeRepo.save(badge);
    }

    public void deleteBadge(Long id) {
        badgeRepo.deleteById(id);
    }

    // ── Freelancer stats update ───────────────────────────────────────────────

    @Transactional
    public LeaderboardEntryDTO updateFreelancerStats(FreelancerStatsDTO dto) {
        FreelancerProfile profile = profileRepo.findByFreelancerEmail(dto.getFreelancerEmail())
                .orElseGet(() -> {
                    FreelancerProfile p = new FreelancerProfile();
                    p.setFreelancerEmail(dto.getFreelancerEmail());
                    return p;
                });

        profile.setFreelancerName(dto.getFreelancerName());
        profile.setMissionsCompleted(dto.getMissionsCompleted());
        profile.setAverageRating(dto.getAverageRating());

        // Save first so profile has a valid ID before badge assignment
        profile = profileRepo.save(profile);
        profileRepo.flush();

        // Compute level and assign badges
        assignBadgesAndLevel(profile);
        profile = profileRepo.save(profile);

        // Refresh leaderboard ranks
        refreshLeaderboardRanks();

        return buildLeaderboardEntry(profile, 0);
    }

    // ── Leaderboard ───────────────────────────────────────────────────────────

    public List<LeaderboardEntryDTO> getLeaderboard() {
        List<FreelancerProfile> profiles = profileRepo.findLeaderboard();
        List<LeaderboardEntryDTO> result = new ArrayList<>();
        for (int i = 0; i < profiles.size(); i++) {
            result.add(buildLeaderboardEntry(profiles.get(i), i + 1));
        }
        return result;
    }

    public LeaderboardEntryDTO getFreelancerProfile(String email) {
        FreelancerProfile profile = profileRepo.findByFreelancerEmail(email)
                .orElseThrow(() -> new RuntimeException("Freelancer not found: " + email));
        return buildLeaderboardEntry(profile, profile.getRankPosition());
    }

    // ── Scheduled refresh every hour ─────────────────────────────────────────

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void scheduledRefresh() {
        log.info("Refreshing badge assignments and leaderboard ranks...");
        profileRepo.findAll().forEach(this::assignBadgesAndLevel);
        refreshLeaderboardRanks();
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private void assignBadgesAndLevel(FreelancerProfile profile) {
        // Compute XP
        int xp = computeXp(profile.getMissionsCompleted(), profile.getAverageRating());
        profile.setTotalXp(xp);

        // Determine level from XP
        BadgeLevel xpLevel = levelFromXp(xp);
        profile.setCurrentLevel(xpLevel);

        // Find all badges the freelancer qualifies for (criteria still apply for special badges)
        List<Badge> eligible = badgeRepo.findByActiveTrue().stream()
                .filter(b -> profile.getMissionsCompleted() >= b.getMinMissionsCompleted()
                        && profile.getAverageRating() >= b.getMinAverageRating())
                .collect(Collectors.toList());

        // Remove old badges first and flush to avoid unique constraint violation
        List<FreelancerBadge> existing = freelancerBadgeRepo.findByFreelancerProfile(profile);
        if (!existing.isEmpty()) {
            freelancerBadgeRepo.deleteAll(existing);
            freelancerBadgeRepo.flush();
        }

        // Re-assign earned badges
        for (Badge badge : eligible) {
            FreelancerBadge fb = new FreelancerBadge();
            fb.setFreelancerProfile(profile);
            fb.setBadge(badge);
            freelancerBadgeRepo.save(fb);
        }

        // Badge name = highest earned badge name
        String highestBadgeName = eligible.stream()
                .max((a, b) -> a.getLevel().ordinal() - b.getLevel().ordinal())
                .map(Badge::getName)
                .orElse(xpLevel != null ? xpLevel.name() : null);

        profile.setCurrentBadgeName(highestBadgeName);
    }

    private void refreshLeaderboardRanks() {
        List<FreelancerProfile> sorted = profileRepo.findLeaderboard();
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setRankPosition(i + 1);
            profileRepo.save(sorted.get(i));
        }
    }

    private LeaderboardEntryDTO buildLeaderboardEntry(FreelancerProfile profile, int rank) {
        List<FreelancerBadge> fbs = freelancerBadgeRepo.findByFreelancerProfile(profile);
        List<BadgeDTO> badgeDTOs = fbs.stream()
                .map(fb -> toDTO(fb.getBadge(), fb.getEarnedAt()))
                .collect(Collectors.toList());

        int xp = profile.getTotalXp();

        // Determine XP bracket for progress bar
        int xpFrom, xpTo;
        String nextLevelName;
        if (xp < XP_BRONZE) {
            xpFrom = 0; xpTo = XP_BRONZE; nextLevelName = "BRONZE";
        } else if (xp < XP_SILVER) {
            xpFrom = XP_BRONZE; xpTo = XP_SILVER; nextLevelName = "SILVER";
        } else if (xp < XP_GOLD) {
            xpFrom = XP_SILVER; xpTo = XP_GOLD; nextLevelName = "GOLD";
        } else if (xp < XP_PLATINUM) {
            xpFrom = XP_GOLD; xpTo = XP_PLATINUM; nextLevelName = "PLATINUM";
        } else {
            xpFrom = XP_PLATINUM; xpTo = XP_PLATINUM; nextLevelName = "MAX";
        }

        int xpProgress = xp - xpFrom;
        int xpRange    = xpTo - xpFrom;
        int percent    = xpRange > 0 ? Math.min(100, (xpProgress * 100) / xpRange) : 100;
        int xpNeeded   = Math.max(0, xpTo - xp);

        LeaderboardEntryDTO dto = new LeaderboardEntryDTO();
        dto.setRank(rank > 0 ? rank : profile.getRankPosition());
        dto.setFreelancerEmail(profile.getFreelancerEmail());
        dto.setFreelancerName(profile.getFreelancerName());
        dto.setMissionsCompleted(profile.getMissionsCompleted());
        dto.setAverageRating(profile.getAverageRating());
        dto.setCurrentLevel(profile.getCurrentLevel());
        dto.setCurrentBadgeName(profile.getCurrentBadgeName());
        dto.setBadges(badgeDTOs);
        dto.setTotalXp(xp);
        dto.setXpForCurrentLevel(xpFrom);
        dto.setXpForNextLevel(xpTo);
        dto.setXpProgress(xpProgress);
        dto.setXpNeeded(xpNeeded);
        dto.setProgressPercent(percent);
        dto.setNextLevelName(nextLevelName);
        return dto;
    }

    private BadgeDTO toDTO(Badge badge, java.time.LocalDateTime earnedAt) {
        BadgeDTO dto = new BadgeDTO();
        dto.setId(badge.getId());
        dto.setName(badge.getName());
        dto.setDescription(badge.getDescription());
        dto.setIconUrl(badge.getIconUrl());
        dto.setLevel(badge.getLevel());
        dto.setMinMissionsCompleted(badge.getMinMissionsCompleted());
        dto.setMinAverageRating(badge.getMinAverageRating());
        dto.setActive(badge.isActive());
        dto.setEarnedAt(earnedAt);
        return dto;
    }

    private BadgeDTO toDTO(Badge badge) {
        return toDTO(badge, null);
    }

    private void mapDtoToEntity(BadgeDTO dto, Badge badge) {
        badge.setName(dto.getName());
        badge.setDescription(dto.getDescription());
        badge.setIconUrl(dto.getIconUrl());
        badge.setLevel(dto.getLevel());
        badge.setMinMissionsCompleted(dto.getMinMissionsCompleted());
        badge.setMinAverageRating(dto.getMinAverageRating());
        badge.setActive(dto.isActive());
    }
}
