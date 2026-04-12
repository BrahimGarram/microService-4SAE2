package com.microservices.contract.service;

import com.microservices.contract.dto.ContractFilterDTO;
import com.microservices.contract.dto.ContractStatsDTO;
import com.microservices.contract.entity.Contract;
import com.microservices.contract.entity.ContractStatus;
import com.microservices.contract.messaging.ContractCreatedEvent;
import com.microservices.contract.messaging.ContractEventPublisher;
import com.microservices.contract.repository.ContractRepository;
import com.microservices.contract.specification.ContractSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractEventPublisher eventPublisher;

    public List<Contract> getAllContracts() { return contractRepository.findAll(); }
    public Optional<Contract> getContractById(Integer id) { return contractRepository.findById(id); }
    public List<Contract> getContractsByStatus(ContractStatus status) { return contractRepository.findByStatus(status); }

    public Contract createContract(Contract contract) {
        if (contract.getStartDate().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Start date must be today or in the future");
        if (!contract.getEndDate().isAfter(contract.getStartDate()))
            throw new IllegalArgumentException("End date must be after start date");
        Contract saved = contractRepository.save(contract);
        // Publish async event to badge-service via RabbitMQ
        eventPublisher.publishContractCreated(new ContractCreatedEvent(
                saved.getIdContract(),
                saved.getFreelancerEmail(),
                saved.getFreelancerName(),
                saved.getContractTitle(),
                saved.getStatus().name()
        ));
        return saved;
    }

    public Contract updateContract(Integer id, Contract d) {
        Contract c = contractRepository.findById(id).orElseThrow(() -> new RuntimeException("Contract not found: " + id));
        c.setContractTitle(d.getContractTitle()); c.setFreelancerName(d.getFreelancerName());
        c.setFreelancerEmail(d.getFreelancerEmail()); c.setClientName(d.getClientName());
        c.setClientEmail(d.getClientEmail()); c.setProjectDescription(d.getProjectDescription());
        c.setStartDate(d.getStartDate()); c.setEndDate(d.getEndDate()); c.setStatus(d.getStatus());
        c.setContractAmount(d.getContractAmount()); c.setPaymentType(d.getPaymentType());
        c.setCurrency(d.getCurrency()); c.setContractType(d.getContractType());
        c.setDeliverables(d.getDeliverables()); c.setTermsAndConditions(d.getTermsAndConditions());
        c.setPaymentTermsDays(d.getPaymentTermsDays());
        return contractRepository.save(c);
    }

    public void deleteContract(Integer id) { contractRepository.deleteById(id); }

    // Advanced queries
    public List<Contract> getExpiringSoon(int days) {
        return contractRepository.findExpiringSoon(LocalDate.now(), LocalDate.now().plusDays(days));
    }
    public List<Contract> getOverdueContracts() { return contractRepository.findOverdueContracts(LocalDate.now()); }
    public List<Contract> searchFullText(String keyword) { return contractRepository.searchFullText(keyword); }
    public List<Contract> getByAmountRange(BigDecimal min, BigDecimal max) { return contractRepository.findByContractAmountBetween(min, max); }
    public List<Contract> getByFreelancerAndStatus(String email, ContractStatus status) { return contractRepository.findByFreelancerEmailIgnoreCaseAndStatus(email, status); }
    public List<Contract> getClientHistoryByAmount(String email) { return contractRepository.findByClientEmailOrderByAmountDesc(email); }

    public Map<String, BigDecimal> getTopEarningFreelancers() {
        return contractRepository.findTopEarningFreelancers().stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (BigDecimal) r[1], (a, b) -> a, LinkedHashMap::new));
    }

    public List<Contract> searchWithFilters(ContractFilterDTO filter) {
        return contractRepository.findAll(ContractSpecification.withFilters(filter));
    }

    public ContractStatsDTO getStats() {
        long total = contractRepository.count();
        Map<String, Long> byStatus = contractRepository.countByStatusGrouped().stream()
                .collect(Collectors.toMap(r -> r[0].toString(), r -> (Long) r[1]));
        return new ContractStatsDTO(total, byStatus, getTopEarningFreelancers(), getExpiringSoon(7).size(), getOverdueContracts().size());
    }
}
