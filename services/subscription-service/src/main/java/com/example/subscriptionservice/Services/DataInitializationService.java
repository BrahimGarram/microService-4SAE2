package com.example.subscriptionservice.Services;

import com.example.subscriptionservice.Entity.BillingCycle;
import com.example.subscriptionservice.Entity.SubscriptionPlan;
import com.example.subscriptionservice.Repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {

    private final SubscriptionPlanRepository planRepository;

    @Override
    public void run(String... args) {
        initializeDefaultPlans();
    }

    private void initializeDefaultPlans() {
        if (planRepository.count() > 0) {
            log.info("Subscription plans already exist, skipping initialization");
            return;
        }

        log.info("Initializing default subscription plans...");

        // Free Plan
        SubscriptionPlan freePlan = new SubscriptionPlan();
        freePlan.setPlanName("FREE");
        freePlan.setDescription("Basic access for new freelancers");
        freePlan.setPrice(BigDecimal.ZERO);
        freePlan.setBillingCycle(BillingCycle.MONTHLY);
        freePlan.setMaxBidsPerMonth(5);
        freePlan.setCommissionRate(new BigDecimal("10.00"));
        freePlan.setHasAnalyticsAccess(false);
        freePlan.setHasProfileBoost(false);
        freePlan.setHasPrioritySupport(false);
        freePlan.setHasFeaturedListing(false);
        freePlan.setMaxProjects(3);
        freePlan.setIsActive(true);
        planRepository.save(freePlan);

        // Basic Plan
        SubscriptionPlan basicPlan = new SubscriptionPlan();
        basicPlan.setPlanName("BASIC");
        basicPlan.setDescription("Perfect for growing freelancers");
        basicPlan.setPrice(new BigDecimal("9.99"));
        basicPlan.setBillingCycle(BillingCycle.MONTHLY);
        basicPlan.setMaxBidsPerMonth(20);
        basicPlan.setCommissionRate(new BigDecimal("7.00"));
        basicPlan.setHasAnalyticsAccess(true);
        basicPlan.setHasProfileBoost(false);
        basicPlan.setHasPrioritySupport(false);
        basicPlan.setHasFeaturedListing(false);
        basicPlan.setMaxProjects(10);
        basicPlan.setIsActive(true);
        planRepository.save(basicPlan);

        // Pro Plan
        SubscriptionPlan proPlan = new SubscriptionPlan();
        proPlan.setPlanName("PRO");
        proPlan.setDescription("Advanced features for professional freelancers");
        proPlan.setPrice(new BigDecimal("29.99"));
        proPlan.setBillingCycle(BillingCycle.MONTHLY);
        proPlan.setMaxBidsPerMonth(100);
        proPlan.setCommissionRate(new BigDecimal("5.00"));
        proPlan.setHasAnalyticsAccess(true);
        proPlan.setHasProfileBoost(true);
        proPlan.setHasPrioritySupport(true);
        proPlan.setHasFeaturedListing(false);
        proPlan.setMaxProjects(50);
        proPlan.setIsActive(true);
        planRepository.save(proPlan);

        // Enterprise Plan
        SubscriptionPlan enterprisePlan = new SubscriptionPlan();
        enterprisePlan.setPlanName("ENTERPRISE");
        enterprisePlan.setDescription("Complete solution for agencies and top freelancers");
        enterprisePlan.setPrice(new BigDecimal("99.99"));
        enterprisePlan.setBillingCycle(BillingCycle.MONTHLY);
        enterprisePlan.setMaxBidsPerMonth(999999); // Unlimited
        enterprisePlan.setCommissionRate(new BigDecimal("3.00"));
        enterprisePlan.setHasAnalyticsAccess(true);
        enterprisePlan.setHasProfileBoost(true);
        enterprisePlan.setHasPrioritySupport(true);
        enterprisePlan.setHasFeaturedListing(true);
        enterprisePlan.setMaxProjects(999999); // Unlimited
        enterprisePlan.setIsActive(true);
        planRepository.save(enterprisePlan);

        log.info("Default subscription plans initialized successfully");
    }
}
