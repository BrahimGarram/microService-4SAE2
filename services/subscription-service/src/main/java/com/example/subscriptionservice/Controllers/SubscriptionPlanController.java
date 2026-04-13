package com.example.subscriptionservice.Controllers;

import com.example.subscriptionservice.Entity.SubscriptionPlan;
import com.example.subscriptionservice.Services.ISubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"}, 
             allowedHeaders = "*", 
             methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
             allowCredentials = "true")
public class SubscriptionPlanController {

    private final ISubscriptionService subscriptionService;

    /**
     * Get all active subscription plans
     */
    @GetMapping
    public ResponseEntity<List<SubscriptionPlan>> getAllActivePlans() {
        try {
            List<SubscriptionPlan> plans = subscriptionService.getAllActivePlans();
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            log.error("Error fetching plans", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get plan by ID
     */
    @GetMapping("/{planId}")
    public ResponseEntity<?> getPlanById(@PathVariable Long planId) {
        try {
            SubscriptionPlan plan = subscriptionService.getPlanById(planId);
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            log.error("Error fetching plan", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Create a new subscription plan (Admin only)
     */
    @PostMapping
    public ResponseEntity<?> createPlan(@RequestBody SubscriptionPlan plan) {
        try {
            SubscriptionPlan created = subscriptionService.createPlan(plan);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error creating plan", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
