package com.freelance.userservice.service;

import com.freelance.userservice.dto.CreateSubscriptionRequest;
import com.freelance.userservice.dto.SubscriptionDTO;
import com.freelance.userservice.dto.SubscriptionPlanDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionClientService {

    private final RestTemplate restTemplate;

    @Value("${subscription.service.url:http://localhost:8091}")
    private String subscriptionServiceUrl;

    /**
     * Get all available subscription plans
     */
    public List<SubscriptionPlanDTO> getAllPlans() {
        try {
            String url = subscriptionServiceUrl + "/api/subscriptions/plans";
            ResponseEntity<List<SubscriptionPlanDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<SubscriptionPlanDTO>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching subscription plans", e);
            throw new RuntimeException("Failed to fetch subscription plans: " + e.getMessage());
        }
    }

    /**
     * Create subscription for a user
     */
    public Map<String, Object> createSubscription(Long userId, CreateSubscriptionRequest request) {
        try {
            String url = subscriptionServiceUrl + "/api/subscriptions/create";
            
            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId);
            body.put("planId", request.getPlanId());
            body.put("autoRenew", request.getAutoRenew());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error creating subscription for user {}", userId, e);
            throw new RuntimeException("Failed to create subscription: " + e.getMessage());
        }
    }

    /**
     * Get active subscription for a user
     */
    public SubscriptionDTO getActiveSubscription(Long userId) {
        try {
            String url = subscriptionServiceUrl + "/api/subscriptions/user/" + userId + "/active";
            ResponseEntity<SubscriptionDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    SubscriptionDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching active subscription for user {}", userId, e);
            return null; // No active subscription
        }
    }

    /**
     * Get subscription history for a user
     */
    public List<SubscriptionDTO> getUserSubscriptionHistory(Long userId) {
        try {
            String url = subscriptionServiceUrl + "/api/subscriptions/user/" + userId + "/history";
            ResponseEntity<List<SubscriptionDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<SubscriptionDTO>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching subscription history for user {}", userId, e);
            throw new RuntimeException("Failed to fetch subscription history: " + e.getMessage());
        }
    }

    /**
     * Cancel subscription for a user
     */
    public Map<String, Object> cancelSubscription(Long userId, String reason) {
        try {
            String url = subscriptionServiceUrl + "/api/subscriptions/cancel/" + userId;
            if (reason != null && !reason.isEmpty()) {
                url += "?reason=" + reason;
            }

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    null,
                    Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error cancelling subscription for user {}", userId, e);
            throw new RuntimeException("Failed to cancel subscription: " + e.getMessage());
        }
    }

    /**
     * Check if user can place a bid
     */
    public Boolean canPlaceBid(Long userId) {
        try {
            String url = subscriptionServiceUrl + "/api/subscriptions/user/" + userId + "/can-bid";
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    Map.class
            );
            Map<String, Object> body = response.getBody();
            return body != null ? (Boolean) body.get("canBid") : false;
        } catch (Exception e) {
            log.error("Error checking bid permission for user {}", userId, e);
            return false;
        }
    }

    /**
     * Increment bid usage for a user
     */
    public void incrementBidUsage(Long userId) {
        try {
            String url = subscriptionServiceUrl + "/api/subscriptions/user/" + userId + "/increment-bid";
            restTemplate.postForEntity(url, null, Map.class);
        } catch (Exception e) {
            log.error("Error incrementing bid usage for user {}", userId, e);
            throw new RuntimeException("Failed to increment bid usage: " + e.getMessage());
        }
    }

    /**
     * Upgrade or downgrade subscription plan
     */
    public Map<String, Object> changePlan(Long userId, Long newPlanId, Boolean immediate) {
        try {
            String url = subscriptionServiceUrl + "/api/subscriptions/change-plan";
            
            Map<String, Object> body = new HashMap<>();
            body.put("userId", userId);
            body.put("newPlanId", newPlanId);
            body.put("immediate", immediate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error changing plan for user {}", userId, e);
            throw new RuntimeException("Failed to change plan: " + e.getMessage());
        }
    }
}
