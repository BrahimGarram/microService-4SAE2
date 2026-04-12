package com.freelancify.badge.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Event received from contract-service when a contract is created.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractCreatedEvent implements Serializable {
    private Integer contractId;
    private String freelancerEmail;
    private String freelancerName;
    private String contractTitle;
    private String status;
}
