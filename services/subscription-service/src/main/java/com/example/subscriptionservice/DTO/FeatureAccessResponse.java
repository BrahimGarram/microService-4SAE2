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
public class FeatureAccessResponse {
    private Boolean hasAccess;
    private String message;
    private Integer remainingBids;
    private BigDecimal commissionRate;
}
