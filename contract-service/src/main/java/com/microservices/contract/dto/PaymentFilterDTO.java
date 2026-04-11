package com.microservices.contract.dto;

import com.microservices.contract.entity.PaymentStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentFilterDTO {
    private PaymentStatus status;
    private String paymentMethod;
    private Integer contractId;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
