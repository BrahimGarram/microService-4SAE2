package com.example.subscriptionservice.Services;

import com.example.subscriptionservice.DTO.PaymentWebhookRequest;
import com.example.subscriptionservice.Entity.Subscription;
import com.example.subscriptionservice.Entity.SubscriptionAction;
import com.example.subscriptionservice.Entity.SubscriptionStatus;
import com.example.subscriptionservice.Repository.SubscriptionHistoryRepository;
import com.example.subscriptionservice.Repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionHistoryRepository historyRepository;
    private final RestTemplate restTemplate;

    /**
     * Initiate payment for a subscription
     */
    public String initiatePayment(Subscription subscription) {
        log.info("Initiating payment for subscription {}", subscription.getId());
        
        // TODO: Integrate with actual payment gateway (Stripe, PayPal, etc.)
        // For now, return a mock payment URL
        
        String paymentUrl = String.format(
                "http://payment-gateway.com/pay?amount=%s&subscriptionId=%s",
                subscription.getAmountPaid(),
                subscription.getId()
        );
        
        return paymentUrl;
    }

    /**
     * Handle payment webhook callback
     */
    @Transactional
    public void handlePaymentWebhook(PaymentWebhookRequest webhook) {
        log.info("Processing payment webhook for subscription {}", webhook.getSubscriptionId());
        
        Subscription subscription = subscriptionRepository.findById(webhook.getSubscriptionId())
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        
        if ("SUCCESS".equalsIgnoreCase(webhook.getStatus())) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setPaymentTransactionId(webhook.getTransactionId());
            subscription.setAmountPaid(webhook.getAmount());
            
            logHistory(subscription, SubscriptionAction.PAYMENT_RECEIVED, 
                      "Payment received successfully", webhook.getAmount(), webhook.getTransactionId());
            
            log.info("Payment successful for subscription {}", subscription.getId());
        } else {
            subscription.setStatus(SubscriptionStatus.PAYMENT_FAILED);
            
            logHistory(subscription, SubscriptionAction.PAYMENT_FAILED, 
                      "Payment failed", null, webhook.getTransactionId());
            
            log.warn("Payment failed for subscription {}", subscription.getId());
        }
        
        subscriptionRepository.save(subscription);
    }

    /**
     * Process renewal payment
     */
    public boolean processRenewalPayment(Subscription subscription) {
        log.info("Processing renewal payment for subscription {}", subscription.getId());
        
        // TODO: Integrate with payment gateway for automatic renewal
        // This would typically use stored payment method
        
        // Mock implementation - simulate 90% success rate
        boolean success = Math.random() < 0.9;
        
        if (success) {
            logHistory(subscription, SubscriptionAction.RENEWED, 
                      "Subscription renewed automatically", 
                      subscription.getPlan().getPrice(), 
                      "RENEWAL-" + System.currentTimeMillis());
        } else {
            logHistory(subscription, SubscriptionAction.PAYMENT_FAILED, 
                      "Renewal payment failed", null, null);
        }
        
        return success;
    }

    private void logHistory(Subscription subscription, SubscriptionAction action, 
                           String description, java.math.BigDecimal amount, String transactionId) {
        com.example.subscriptionservice.Entity.SubscriptionHistory history = 
                new com.example.subscriptionservice.Entity.SubscriptionHistory();
        history.setUserId(subscription.getUserId());
        history.setSubscriptionId(subscription.getId());
        history.setPlan(subscription.getPlan());
        history.setAction(action);
        history.setDescription(description);
        history.setAmount(amount);
        history.setTransactionId(transactionId);
        historyRepository.save(history);
    }
}
