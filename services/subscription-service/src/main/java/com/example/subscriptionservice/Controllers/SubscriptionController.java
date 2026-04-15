package com.example.subscriptionservice.Controllers;

import com.example.subscriptionservice.DTO.*;
import com.example.subscriptionservice.Entity.Subscription;
import com.example.subscriptionservice.Services.ISubscriptionService;
import com.example.subscriptionservice.Services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"}, 
             allowedHeaders = "*", 
             methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
             allowCredentials = "true")
public class SubscriptionController {

    private final ISubscriptionService subscriptionService;
    private final PaymentService paymentService;

    /**
     * Create a new subscription (initiates payment)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createSubscription(@RequestBody SubscriptionRequest request) {
        try {
            log.info("Creating subscription for user: {}", request.getUserId());
            
            Subscription subscription = subscriptionService.createSubscription(request);
            String paymentUrl = paymentService.initiatePayment(subscription);
            
            Map<String, Object> response = new HashMap<>();
            response.put("subscription", subscription);
            response.put("paymentUrl", paymentUrl);
            response.put("message", "Subscription created. Please complete payment.");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error creating subscription", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get user's active subscription
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<?> getActiveSubscription(@PathVariable Long userId) {
        try {
            Subscription subscription = subscriptionService.getActiveSubscription(userId);
            
            if (subscription == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No active subscription found"));
            }
            
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            log.error("Error fetching active subscription", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get user's subscription history
     */
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<?> getUserSubscriptionHistory(@PathVariable Long userId) {
        try {
            List<Subscription> history = subscriptionService.getUserSubscriptionHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error fetching subscription history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all subscriptions (shows all users with subscriptions)
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllSubscriptions() {
        try {
            List<Subscription> subscriptions = subscriptionService.getAllSubscriptions();
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            log.error("Error fetching all subscriptions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cancel subscription
     */
    @PostMapping("/cancel/{userId}")
    public ResponseEntity<?> cancelSubscription(
            @PathVariable Long userId,
            @RequestParam(required = false) String reason) {
        try {
            Subscription cancelled = subscriptionService.cancelSubscription(
                    userId, reason != null ? reason : "User requested cancellation");
            
            return ResponseEntity.ok(Map.of(
                    "message", "Subscription cancelled successfully",
                    "subscription", cancelled
            ));
        } catch (Exception e) {
            log.error("Error cancelling subscription", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Upgrade or downgrade subscription
     */
    @PostMapping("/change-plan")
    public ResponseEntity<?> changePlan(@RequestBody UpgradeDowngradeRequest request) {
        try {
            Subscription updated = subscriptionService.upgradeOrDowngrade(request);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Plan changed successfully",
                    "subscription", updated
            ));
        } catch (Exception e) {
            log.error("Error changing plan", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Validate feature access (for other microservices)
     */
    @PostMapping("/validate-access")
    public ResponseEntity<FeatureAccessResponse> validateFeatureAccess(
            @RequestBody FeatureAccessRequest request) {
        try {
            FeatureAccessResponse response = subscriptionService.validateFeatureAccess(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating feature access", e);
            FeatureAccessResponse errorResponse = new FeatureAccessResponse();
            errorResponse.setHasAccess(false);
            errorResponse.setMessage("Error validating access");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Check if user can place a bid
     */
    @GetMapping("/user/{userId}/can-bid")
    public ResponseEntity<?> canPlaceBid(@PathVariable Long userId) {
        try {
            Boolean canBid = subscriptionService.canPlaceBid(userId);
            return ResponseEntity.ok(Map.of("canBid", canBid));
        } catch (Exception e) {
            log.error("Error checking bid permission", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Increment bid usage (called by project service after successful bid)
     */
    @PostMapping("/user/{userId}/increment-bid")
    public ResponseEntity<?> incrementBidUsage(@PathVariable Long userId) {
        try {
            subscriptionService.incrementBidUsage(userId);
            return ResponseEntity.ok(Map.of("message", "Bid usage incremented"));
        } catch (Exception e) {
            log.error("Error incrementing bid usage", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all unique users who have subscriptions
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<Subscription> subscriptions = subscriptionService.getAllSubscriptions();
            
            // Extract unique user IDs with their latest subscription info
            Map<Long, Map<String, Object>> usersMap = new HashMap<>();
            
            for (Subscription sub : subscriptions) {
                Long userId = sub.getUserId();
                if (!usersMap.containsKey(userId) || 
                    sub.getCreatedAt().isAfter(
                        ((Subscription)usersMap.get(userId).get("subscription")).getCreatedAt())) {
                    
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("userId", userId);
                    userInfo.put("subscription", sub);
                    userInfo.put("currentPlan", sub.getPlan().getPlanName());
                    userInfo.put("status", sub.getStatus());
                    userInfo.put("hasActiveSubscription", sub.isActive());
                    
                    usersMap.put(userId, userInfo);
                }
            }
            
            return ResponseEntity.ok(usersMap.values());
        } catch (Exception e) {
            log.error("Error fetching users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get user summary (all subscriptions for a user)
     */
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<?> getUserSummary(@PathVariable Long userId) {
        try {
            List<Subscription> history = subscriptionService.getUserSubscriptionHistory(userId);
            Subscription active = subscriptionService.getActiveSubscription(userId);
            
            Map<String, Object> summary = new HashMap<>();
            summary.put("userId", userId);
            summary.put("activeSubscription", active);
            summary.put("totalSubscriptions", history.size());
            summary.put("subscriptionHistory", history);
            
            if (active != null) {
                summary.put("currentPlan", active.getPlan().getPlanName());
                summary.put("bidsRemaining", active.getPlan().getMaxBidsPerMonth() - active.getBidsUsedThisMonth());
                summary.put("bidsUsed", active.getBidsUsedThisMonth());
            }
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error fetching user summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
