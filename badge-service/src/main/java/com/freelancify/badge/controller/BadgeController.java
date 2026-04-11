package com.freelancify.badge.controller;

import com.freelancify.badge.dto.BadgeDTO;
import com.freelancify.badge.dto.FreelancerStatsDTO;
import com.freelancify.badge.dto.LeaderboardEntryDTO;
import com.freelancify.badge.entity.Badge;
import com.freelancify.badge.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class BadgeController {

    private final BadgeService badgeService;

    // ── Badge management (Admin) ──────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<Badge>> getAllBadges() {
        return ResponseEntity.ok(badgeService.getAllBadges());
    }

    @PostMapping
    public ResponseEntity<Badge> createBadge(@RequestBody BadgeDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(badgeService.createBadge(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Badge> updateBadge(@PathVariable Long id, @RequestBody BadgeDTO dto) {
        return ResponseEntity.ok(badgeService.updateBadge(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBadge(@PathVariable Long id) {
        badgeService.deleteBadge(id);
        return ResponseEntity.noContent().build();
    }

    // ── Freelancer stats update ───────────────────────────────────────────────

    @PostMapping("/update-stats")
    public ResponseEntity<LeaderboardEntryDTO> updateStats(@RequestBody FreelancerStatsDTO dto) {
        return ResponseEntity.ok(badgeService.updateFreelancerStats(dto));
    }

    // ── Leaderboard ───────────────────────────────────────────────────────────

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDTO>> getLeaderboard() {
        return ResponseEntity.ok(badgeService.getLeaderboard());
    }

    @GetMapping("/freelancer/{email}")
    public ResponseEntity<LeaderboardEntryDTO> getFreelancerProfile(@PathVariable String email) {
        try {
            return ResponseEntity.ok(badgeService.getFreelancerProfile(email));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
