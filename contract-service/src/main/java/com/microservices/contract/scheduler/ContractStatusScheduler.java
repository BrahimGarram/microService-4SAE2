package com.microservices.contract.scheduler;

import com.microservices.contract.entity.Contract;
import com.microservices.contract.entity.ContractStatus;
import com.microservices.contract.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContractStatusScheduler {
    
    private final ContractRepository contractRepository;
    
    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateExpiredContracts() {
        log.info("Running scheduled task to update expired contracts");
        
        LocalDate today = LocalDate.now();
        List<Contract> allContracts = contractRepository.findAll();
        
        int updatedCount = 0;
        for (Contract contract : allContracts) {
            // If end date has passed and status is PENDING, change to CANCELLED
            if (contract.getEndDate().isBefore(today) && 
                contract.getStatus() == ContractStatus.PENDING) {
                contract.setStatus(ContractStatus.CANCELLED);
                contractRepository.save(contract);
                updatedCount++;
                log.info("Contract ID {} status changed from PENDING to CANCELLED", contract.getIdContract());
            }
            // If end date has passed and status is ACTIVE, change to EXPIRED
            else if (contract.getEndDate().isBefore(today) && 
                     contract.getStatus() == ContractStatus.ACTIVE) {
                contract.setStatus(ContractStatus.EXPIRED);
                contractRepository.save(contract);
                updatedCount++;
                log.info("Contract ID {} status changed from ACTIVE to EXPIRED", contract.getIdContract());
            }
        }
        
        log.info("Scheduled task completed. Updated {} contracts", updatedCount);
    }
    
    // Run every hour to check for expired contracts
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void checkExpiredContractsHourly() {
        log.info("Running hourly check for expired contracts");
        updateExpiredContracts();
    }
}
