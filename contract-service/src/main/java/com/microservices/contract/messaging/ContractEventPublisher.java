package com.microservices.contract.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes contract events to RabbitMQ.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContractEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishContractCreated(ContractCreatedEvent event) {
        log.info("Publishing contract.created event for contract: {}", event.getContractId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CONTRACT_EXCHANGE,
                RabbitMQConfig.CONTRACT_ROUTING_KEY,
                event
        );
    }
}
