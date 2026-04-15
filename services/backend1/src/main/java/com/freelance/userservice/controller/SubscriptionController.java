package com.freelance.userservice.controller;

import com.freelance.userservice.dto.CreateSubscriptionRequest;
import com.freelance.userservice.dto.SubscriptionDTO;
import com.freelance.userservice.dto.SubscriptionPlanDTO;
import com.freelance.userservice.service.SubscriptionClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionClientService subscriptionClientService;

    /**
     * Get all available subscription plans
     */
    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlanDTO>> getAllPlans() {
        try {
            List<SubscriptionPlanDTO> plans = subscriptionClientService.getAllPlans();
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            log.error("Error fetching plans", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get current user's active subscription
     */
    @GetMapping("/my-subscription")
    public ResponseEntity<?> getMySubscription(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            SubscriptionDTO subscription = subscriptionClientService.getActiveSubscription(userId);
            
            if (subscription == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No active subscription found"));
            }
            
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            log.error("Error fetching user subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get current user's subscription history
     */
    @GetMapping("/my-history")
    public ResponseEntity<?> getMyHistory(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            List<SubscriptionDTO> history = subscriptionClientService.getUserSubscriptionHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error fetching subscription history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create subscription for current user
     */
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(
            @RequestBody CreateSubscriptionRequest request,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            Map<String, Object> result = subscriptionClientService.createSubscription(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            log.error("Error creating subscription", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cancel current user's subscription
     */
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelSubscription(
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            Map<String, Object> result = subscriptionClientService.cancelSubscription(userId, reason);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error cancelling subscription", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Change subscription plan
     */
    @PostMapping("/change-plan")
    public ResponseEntity<?> changePlan(
            @RequestParam Long newPlanId,
            @RequestParam(defaultValue = "true") Boolean immediate,
            Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            Map<String, Object> result = subscriptionClientService.changePlan(userId, newPlanId, immediate);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error changing plan", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Check if current user can place a bid
     */
    @GetMapping("/can-bid")
    public ResponseEntity<Map<String, Boolean>> canPlaceBid(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            Boolean canBid = subscriptionClientService.canPlaceBid(userId);
            return ResponseEntity.ok(Map.of("canBid", canBid));
        } catch (Exception e) {
            log.error("Error checking bid permission", e);
            return ResponseEntity.ok(Map.of("canBid", false));
        }
    }

    /**
     * Increment bid usage (called by project service after successful bid)
     */
    @PostMapping("/increment-bid")
    public ResponseEntity<?> incrementBid(Authentication authentication) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            subscriptionClientService.incrementBidUsage(userId);
            return ResponseEntity.ok(Map.of("message", "Bid usage incremented"));
        } catch (Exception e) {
            log.error("Error incrementing bid", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Admin: Get subscription for any user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserSubscription(@PathVariable Long userId) {
        try {
            SubscriptionDTO subscription = subscriptionClientService.getActiveSubscription(userId);
            
            if (subscription == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No active subscription found"));
            }
            
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            log.error("Error fetching user subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Extract user ID from JWT token
     */
    private Long getUserIdFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String userIdStr = jwt.getClaimAsString("user_id");
            if (userIdStr != null) {
                return Long.parseLong(userIdStr);
            }
        }
        throw new RuntimeException("Unable to extract user ID from authentication");
    }
}
