package com.microservices.contract.dto;

import lombok.Data;

@Data
public class ContractDisputeDTO {
    private String reason;
    private String raisedBy;   // "CLIENT" or "FREELANCER"
    private String description;
}
