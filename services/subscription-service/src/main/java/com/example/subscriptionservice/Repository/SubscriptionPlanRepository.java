package com.example.subscriptionservice.Repository;

import com.example.subscriptionservice.Entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    
    Optional<SubscriptionPlan> findByPlanName(String planName);
    
    List<SubscriptionPlan> findByIsActiveTrue();
}
