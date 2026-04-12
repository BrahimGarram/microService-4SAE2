package com.microservices.contract.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Event published to RabbitMQ when a contract is created.
 * Consumed by badge-service to update freelancer stats.
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
