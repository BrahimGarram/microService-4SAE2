package com.microservices.contract.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripePaymentResponseDTO {
    private String paymentIntentId;
    private String clientSecret;
    private String status;
    private Integer paymentId; // saved Payment entity id
}
