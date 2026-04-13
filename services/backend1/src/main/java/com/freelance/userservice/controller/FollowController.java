package com.freelance.userservice.controller;

import com.freelance.userservice.service.FollowService;
import com.freelance.userservice.service.UserService;
import com.freelance.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final UserService userService;

    /**
     * GET /api/follow/following
     * Returns the list of user IDs that the current user follows (for feed "abonnements").
     */
    @GetMapping("/following")
    public ResponseEntity<List<Long>> getFollowing(Authentication authentication) {
        String keycloakId = JwtUtil.extractKeycloakId((Jwt) authentication.getPrincipal());
        Long currentUserId = userService.getCurrentUser(keycloakId).getId();
        List<Long> followingIds = followService.getFollowingUserIds(currentUserId);
        return ResponseEntity.ok(followingIds);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Void> follow(@PathVariable Long userId, Authentication authentication) {
        String keycloakId = JwtUtil.extractKeycloakId((Jwt) authentication.getPrincipal());
        followService.follow(userId, keycloakId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> unfollow(@PathVariable Long userId, Authentication authentication) {
        String keycloakId = JwtUtil.extractKeycloakId((Jwt) authentication.getPrincipal());
        followService.unfollow(userId, keycloakId);
        return ResponseEntity.ok().build();
    }
}
