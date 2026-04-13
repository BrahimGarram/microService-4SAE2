package com.example.subscriptionservice.Services;

import com.example.subscriptionservice.DTO.*;
import com.example.subscriptionservice.Entity.Subscription;
import com.example.subscriptionservice.Entity.SubscriptionPlan;

import java.util.List;

public interface ISubscriptionService {
    
    // Plan management
    SubscriptionPlan createPlan(SubscriptionPlan plan);
    List<SubscriptionPlan> getAllActivePlans();
    SubscriptionPlan getPlanById(Long planId);
    
    // Subscription lifecycle
    Subscription createSubscription(SubscriptionRequest request);
    Subscription activateSubscription(Long subscriptionId, String transactionId);
    Subscription cancelSubscription(Long userId, String reason);
    
    // Upgrade/Downgrade
    Subscription upgradeOrDowngrade(UpgradeDowngradeRequest request);
    
    // Feature access validation
    FeatureAccessResponse validateFeatureAccess(FeatureAccessRequest request);
    Boolean canPlaceBid(Long userId);
    void incrementBidUsage(Long userId);
    
    // User subscription info
    Subscription getActiveSubscription(Long userId);
    List<Subscription> getUserSubscriptionHistory(Long userId);
}
