package com.microservices.contract.dto;

import com.microservices.contract.entity.ContractStatus;
import com.microservices.contract.entity.ContractType;
import com.microservices.contract.entity.PaymentType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContractFilterDTO {
    private ContractStatus status;
    private ContractType contractType;
    private PaymentType paymentType;
    private String freelancerEmail;
    private String clientEmail;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private LocalDate startDateFrom;
    private LocalDate startDateTo;
    private LocalDate endDateFrom;
    private LocalDate endDateTo;
    private String keyword; // searches title + description + deliverables
}
