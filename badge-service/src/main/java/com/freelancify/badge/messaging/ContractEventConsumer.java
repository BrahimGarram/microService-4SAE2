package com.freelancify.badge.messaging;

import com.freelancify.badge.dto.FreelancerStatsDTO;
import com.freelancify.badge.service.BadgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens for contract.created events from contract-service.
 * Increments the freelancer's mission count and re-evaluates badges.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContractEventConsumer {

    private final BadgeService badgeService;

    @RabbitListener(queues = "contract.created.queue")
    public void handleContractCreated(ContractCreatedEvent event) {
        log.info("Received contract.created event for freelancer: {}", event.getFreelancerEmail());
        try {
            // Build a stats update: increment missions by 1, keep existing rating
            FreelancerStatsDTO stats = new FreelancerStatsDTO();
            stats.setFreelancerEmail(event.getFreelancerEmail());
            stats.setFreelancerName(event.getFreelancerName());
            // Increment missions completed
            stats.setMissionsCompleted(1);
            stats.setAverageRating(0.0);
            badgeService.updateFreelancerStats(stats);
            log.info("Badge stats updated for freelancer: {}", event.getFreelancerEmail());
        } catch (Exception e) {
            log.error("Failed to update badge stats for freelancer {}: {}", event.getFreelancerEmail(), e.getMessage());
        }
    }
}
