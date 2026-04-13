package com.example.subscriptionservice.Services;

import com.example.subscriptionservice.Entity.Subscription;
import com.example.subscriptionservice.Entity.SubscriptionAction;
import com.example.subscriptionservice.Entity.SubscriptionStatus;
import com.example.subscriptionservice.Repository.SubscriptionHistoryRepository;
import com.example.subscriptionservice.Repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduledTasks {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository historyRepository;
    private final PaymentService paymentService;

    /**
     * Check for expiring subscriptions every hour
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void checkExpiringSubscriptions() {
        log.info("Checking for expiring subscriptions...");
        
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expiringSubscriptions = subscriptionRepository
                .findExpiringSubscriptions(now);
        
        for (Subscription subscription : expiringSubscriptions) {
            if (subscription.getEndDate().isBefore(now)) {
                subscription.setStatus(SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(subscription);
                
                log.info("Subscription {} expired for user {}", 
                        subscription.getId(), subscription.getUserId());
            }
        }
    }

    /**
     * Attempt to renew subscriptions 3 days before expiration
     */
    @Scheduled(cron = "0 0 8 * * *") // Daily at 8 AM
    @Transactional
    public void attemptAutoRenewal() {
        log.info("Attempting auto-renewal for subscriptions...");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime renewalWindow = now.plusDays(3);
        
        List<Subscription> subscriptionsForRenewal = subscriptionRepository
                .findSubscriptionsForRenewal(now, renewalWindow);
        
        for (Subscription subscription : subscriptionsForRenewal) {
            // Avoid multiple renewal attempts on the same day
            if (subscription.getLastRenewalAttempt() != null &&
                subscription.getLastRenewalAttempt().toLocalDate().equals(now.toLocalDate())) {
                continue;
            }
            
            try {
                log.info("Attempting renewal for subscription {}", subscription.getId());
                
                // Initiate payment
                boolean paymentSuccess = paymentService.processRenewalPayment(subscription);
                
                subscription.setLastRenewalAttempt(now);
                
                if (paymentSuccess) {
                    // Extend subscription
                    LocalDateTime newEndDate = calculateNewEndDate(
                            subscription.getEndDate(), 
                            subscription.getPlan().getBillingCycle());
                    
                    subscription.setEndDate(newEndDate);
                    subscription.setStartDate(subscription.getEndDate());
                    subscriptionRepository.save(subscription);
                    
                    log.info("Subscription {} renewed successfully", subscription.getId());
                } else {
                    subscription.setStatus(SubscriptionStatus.PAYMENT_FAILED);
                    subscriptionRepository.save(subscription);
                    
                    log.warn("Payment failed for subscription {}", subscription.getId());
                }
                
            } catch (Exception e) {
                log.error("Error renewing subscription {}", subscription.getId(), e);
                subscription.setLastRenewalAttempt(now);
                subscriptionRepository.save(subscription);
            }
        }
    }

    /**
     * Send renewal reminders 7 days before expiration
     */
    @Scheduled(cron = "0 0 9 * * *") // Daily at 9 AM
    public void sendRenewalReminders() {
        log.info("Sending renewal reminders...");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderDate = now.plusDays(7);
        
        List<Subscription> subscriptions = subscriptionRepository
                .findSubscriptionsForRenewal(reminderDate.minusDays(1), reminderDate.plusDays(1));
        
        for (Subscription subscription : subscriptions) {
            // Send reminder notification (integrate with notification service)
            log.info("Sending renewal reminder for subscription {} to user {}", 
                    subscription.getId(), subscription.getUserId());
        }
    }

    private LocalDateTime calculateNewEndDate(LocalDateTime currentEndDate, 
                                             com.example.subscriptionservice.Entity.BillingCycle cycle) {
        return switch (cycle) {
            case MONTHLY -> currentEndDate.plusMonths(1);
            case QUARTERLY -> currentEndDate.plusMonths(3);
            case YEARLY -> currentEndDate.plusYears(1);
        };
    }
}
