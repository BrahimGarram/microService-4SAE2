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
@RequestMapping("")  // Empty because context-path already includes /api/subscriptions
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
}
