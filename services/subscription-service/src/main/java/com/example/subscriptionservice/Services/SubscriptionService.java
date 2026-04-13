package com.example.subscriptionservice.Services;

import com.example.subscriptionservice.DTO.*;
import com.example.subscriptionservice.Entity.*;
import com.example.subscriptionservice.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService implements ISubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionHistoryRepository historyRepository;

    @Override
    public SubscriptionPlan createPlan(SubscriptionPlan plan) {
        log.info("Creating new subscription plan: {}", plan.getPlanName());
        return planRepository.save(plan);
    }

    @Override
    public List<SubscriptionPlan> getAllActivePlans() {
        return planRepository.findByIsActiveTrue();
    }

    @Override
    public SubscriptionPlan getPlanById(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found with id: " + planId));
    }

    @Override
    @Transactional
    public Subscription createSubscription(SubscriptionRequest request) {
        log.info("Creating subscription for user: {}", request.getUserId());
        
        // Check if user already has an active subscription
        subscriptionRepository.findActiveSubscriptionByUserId(request.getUserId())
                .ifPresent(sub -> {
                    throw new RuntimeException("User already has an active subscription");
                });

        SubscriptionPlan plan = getPlanById(request.getPlanId());
        
        Subscription subscription = new Subscription();
        subscription.setUserId(request.getUserId());
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.PENDING_PAYMENT);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(calculateEndDate(LocalDateTime.now(), plan.getBillingCycle()));
        subscription.setAutoRenew(request.getAutoRenew());
        subscription.setAmountPaid(plan.getPrice());
        
        Subscription saved = subscriptionRepository.save(subscription);
        
        // Log history
        logHistory(saved.getUserId(), saved.getId(), plan, SubscriptionAction.CREATED, 
                   "Subscription created, pending payment", plan.getPrice(), null);
        
        return saved;
    }

    @Override
    @Transactional
    public Subscription activateSubscription(Long subscriptionId, String transactionId) {
        log.info("Activating subscription: {}", subscriptionId);
        
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setPaymentTransactionId(transactionId);
        
        Subscription activated = subscriptionRepository.save(subscription);
        
        logHistory(activated.getUserId(), activated.getId(), activated.getPlan(), 
                   SubscriptionAction.ACTIVATED, "Subscription activated successfully", 
                   activated.getAmountPaid(), transactionId);
        
        return activated;
    }

    @Override
    @Transactional
    public Subscription cancelSubscription(Long userId, String reason) {
        log.info("Cancelling subscription for user: {}", userId);
        
        Subscription subscription = subscriptionRepository.findActiveSubscriptionByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No active subscription found"));
        
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setCancellationReason(reason);
        subscription.setAutoRenew(false);
        
        Subscription cancelled = subscriptionRepository.save(subscription);
        
        logHistory(userId, subscription.getId(), subscription.getPlan(), 
                   SubscriptionAction.CANCELLED, "Subscription cancelled: " + reason, null, null);
        
        return cancelled;
    }

    @Override
    @Transactional
    public Subscription upgradeOrDowngrade(UpgradeDowngradeRequest request) {
        log.info("Processing plan change for user: {}", request.getUserId());
        
        Subscription currentSub = subscriptionRepository.findActiveSubscriptionByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("No active subscription found"));
        
        SubscriptionPlan newPlan = getPlanById(request.getNewPlanId());
        SubscriptionPlan oldPlan = currentSub.getPlan();
        
        boolean isUpgrade = newPlan.getPrice().compareTo(oldPlan.getPrice()) > 0;
        
        if (request.getImmediate()) {
            // Apply immediately with proration
            BigDecimal prorationAmount = calculateProration(currentSub, newPlan);
            currentSub.setPlan(newPlan);
            currentSub.setAmountPaid(prorationAmount);
            
            logHistory(request.getUserId(), currentSub.getId(), newPlan, 
                       isUpgrade ? SubscriptionAction.UPGRADED : SubscriptionAction.DOWNGRADED,
                       String.format("Plan changed from %s to %s", oldPlan.getPlanName(), newPlan.getPlanName()),
                       prorationAmount, null);
        } else {
            // Schedule for next billing cycle
            currentSub.setPlan(newPlan);
            logHistory(request.getUserId(), currentSub.getId(), newPlan,
                       isUpgrade ? SubscriptionAction.UPGRADED : SubscriptionAction.DOWNGRADED,
                       "Plan change scheduled for next billing cycle", null, null);
        }
        
        return subscriptionRepository.save(currentSub);
    }

    @Override
    public FeatureAccessResponse validateFeatureAccess(FeatureAccessRequest request) {
        FeatureAccessResponse response = new FeatureAccessResponse();
        
        try {
            Subscription subscription = subscriptionRepository.findActiveSubscriptionByUserId(request.getUserId())
                    .orElse(null);
            
            if (subscription == null || !subscription.isActive()) {
                response.setHasAccess(false);
                response.setMessage("No active subscription");
                return response;
            }
            
            SubscriptionPlan plan = subscription.getPlan();
            
            switch (request.getFeatureName().toUpperCase()) {
                case "BID":
                    response.setHasAccess(canPlaceBid(request.getUserId()));
                    response.setRemainingBids(plan.getMaxBidsPerMonth() - subscription.getBidsUsedThisMonth());
                    response.setMessage(response.getHasAccess() ? "Bid allowed" : "Bid limit reached");
                    break;
                case "ANALYTICS":
                    response.setHasAccess(plan.getHasAnalyticsAccess());
                    response.setMessage(response.getHasAccess() ? "Analytics access granted" : "Upgrade required");
                    break;
                case "PROFILE_BOOST":
                    response.setHasAccess(plan.getHasProfileBoost());
                    response.setMessage(response.getHasAccess() ? "Profile boost available" : "Upgrade required");
                    break;
                case "COMMISSION":
                    response.setHasAccess(true);
                    response.setCommissionRate(plan.getCommissionRate());
                    response.setMessage("Commission rate: " + plan.getCommissionRate() + "%");
                    break;
                default:
                    response.setHasAccess(false);
                    response.setMessage("Unknown feature");
            }
            
        } catch (Exception e) {
            log.error("Error validating feature access", e);
            response.setHasAccess(false);
            response.setMessage("Error validating access");
        }
        
        return response;
    }

    @Override
    public Boolean canPlaceBid(Long userId) {
        return subscriptionRepository.findActiveSubscriptionByUserId(userId)
                .map(sub -> {
                    resetBidsIfNewMonth(sub);
                    return sub.getBidsUsedThisMonth() < sub.getPlan().getMaxBidsPerMonth();
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public void incrementBidUsage(Long userId) {
        subscriptionRepository.findActiveSubscriptionByUserId(userId)
                .ifPresent(sub -> {
                    resetBidsIfNewMonth(sub);
                    sub.setBidsUsedThisMonth(sub.getBidsUsedThisMonth() + 1);
                    subscriptionRepository.save(sub);
                });
    }

    @Override
    public Subscription getActiveSubscription(Long userId) {
        return subscriptionRepository.findActiveSubscriptionByUserId(userId)
                .orElse(null);
    }

    @Override
    public List<Subscription> getUserSubscriptionHistory(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    // Helper methods
    private LocalDateTime calculateEndDate(LocalDateTime startDate, BillingCycle cycle) {
        return switch (cycle) {
            case MONTHLY -> startDate.plusMonths(1);
            case QUARTERLY -> startDate.plusMonths(3);
            case YEARLY -> startDate.plusYears(1);
        };
    }

    private BigDecimal calculateProration(Subscription currentSub, SubscriptionPlan newPlan) {
        // Simple proration logic - can be enhanced
        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDateTime.now(), currentSub.getEndDate());
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(
                currentSub.getStartDate(), currentSub.getEndDate());
        
        if (totalDays == 0) {
            return newPlan.getPrice();
        }
        
        BigDecimal unusedAmount = currentSub.getAmountPaid()
                .multiply(BigDecimal.valueOf(daysRemaining))
                .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP);
        
        return newPlan.getPrice().subtract(unusedAmount);
    }

    private void resetBidsIfNewMonth(Subscription subscription) {
        LocalDateTime now = LocalDateTime.now();
        if (subscription.getLastBidResetDate().getMonth() != now.getMonth() ||
            subscription.getLastBidResetDate().getYear() != now.getYear()) {
            subscription.setBidsUsedThisMonth(0);
            subscription.setLastBidResetDate(now);
            subscriptionRepository.save(subscription);
        }
    }

    private void logHistory(Long userId, Long subscriptionId, SubscriptionPlan plan,
                           SubscriptionAction action, String description, 
                           BigDecimal amount, String transactionId) {
        SubscriptionHistory history = new SubscriptionHistory();
        history.setUserId(userId);
        history.setSubscriptionId(subscriptionId);
        history.setPlan(plan);
        history.setAction(action);
        history.setDescription(description);
        history.setAmount(amount);
        history.setTransactionId(transactionId);
        historyRepository.save(history);
    }
}
