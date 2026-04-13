package com.example.subscriptionservice.Repository;

import com.example.subscriptionservice.Entity.Subscription;
import com.example.subscriptionservice.Entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    Optional<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
    
    List<Subscription> findByUserId(Long userId);
    
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate <= :expirationDate")
    List<Subscription> findExpiringSubscriptions(LocalDateTime expirationDate);
    
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.autoRenew = true AND s.endDate BETWEEN :startDate AND :endDate")
    List<Subscription> findSubscriptionsForRenewal(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.status = 'ACTIVE' ORDER BY s.endDate DESC")
    Optional<Subscription> findActiveSubscriptionByUserId(Long userId);
}
