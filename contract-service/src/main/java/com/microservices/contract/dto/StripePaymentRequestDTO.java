package com.microservices.contract.dto;

import lombok.Data;

@Data
public class StripePaymentRequestDTO {
    private Integer contractId;
    private String currency;       // e.g. "usd"
    private String description;
    private String customerEmail;
    private String paymentMethodId; // from Stripe.js on frontend
}
