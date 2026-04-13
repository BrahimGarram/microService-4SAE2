package com.example.subscriptionservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentWebhookRequest {
    private String transactionId;
    private Long subscriptionId;
    private String status; // SUCCESS, FAILED
    private BigDecimal amount;
    private String paymentMethod;
}
