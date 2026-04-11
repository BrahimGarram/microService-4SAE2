package com.microservices.contract.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContractRenewalDTO {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate newEndDate;
    private BigDecimal newAmount;
    private String renewalNote;
}
