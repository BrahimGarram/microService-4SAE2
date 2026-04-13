package com.example.subscriptionservice.Controllers;

import com.example.subscriptionservice.DTO.PaymentWebhookRequest;
import com.example.subscriptionservice.Services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PaymentWebhookController {

    private final PaymentService paymentService;

    /**
     * Handle payment gateway webhook
     */
    @PostMapping("/payment")
    public ResponseEntity<?> handlePaymentWebhook(@RequestBody PaymentWebhookRequest webhook) {
        try {
            log.info("Received payment webhook for subscription: {}", webhook.getSubscriptionId());
            
            // TODO: Verify webhook signature for security
            
            paymentService.handlePaymentWebhook(webhook);
            
            return ResponseEntity.ok(Map.of("message", "Webhook processed successfully"));
        } catch (Exception e) {
            log.error("Error processing payment webhook", e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to process webhook"));
        }
    }
}
