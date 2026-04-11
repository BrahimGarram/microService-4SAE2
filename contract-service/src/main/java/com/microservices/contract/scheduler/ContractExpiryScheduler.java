package com.microservices.contract.scheduler;

import com.microservices.contract.service.ContractMetierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContractExpiryScheduler {

    private final ContractMetierService metierService;

    // Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void expireOverdueContracts() {
        int count = metierService.autoExpireOverdueContracts();
        log.info("[Scheduler] Auto-expired {} overdue contracts.", count);
    }
}
