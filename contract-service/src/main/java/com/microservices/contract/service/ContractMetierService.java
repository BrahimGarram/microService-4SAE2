package com.microservices.contract.service;

import com.microservices.contract.dto.*;
import com.microservices.contract.entity.Contract;
import com.microservices.contract.entity.ContractStatus;
import com.microservices.contract.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractMetierService {

    private final ContractRepository contractRepository;

    // ── 1. Activate a PENDING contract ────────────────────────────────────────
    public Contract activateContract(Integer id) {
        Contract c = findOrThrow(id);
        if (c.getStatus() != ContractStatus.PENDING)
            throw new IllegalStateException("Only PENDING contracts can be activated. Current: " + c.getStatus());
        c.setStatus(ContractStatus.ACTIVE);
        return contractRepository.save(c);
    }

    // ── 2. Complete a contract ─────────────────────────────────────────────────
    public Contract completeContract(Integer id) {
        Contract c = findOrThrow(id);
        if (c.getStatus() != ContractStatus.ACTIVE)
            throw new IllegalStateException("Only ACTIVE contracts can be completed. Current: " + c.getStatus());
        c.setStatus(ContractStatus.COMPLETED);
        return contractRepository.save(c);
    }

    // ── 3. Cancel a contract ───────────────────────────────────────────────────
    public Contract cancelContract(Integer id, String reason) {
        Contract c = findOrThrow(id);
        if (c.getStatus() == ContractStatus.COMPLETED || c.getStatus() == ContractStatus.CANCELLED)
            throw new IllegalStateException("Cannot cancel a " + c.getStatus() + " contract.");
        c.setStatus(ContractStatus.CANCELLED);
        if (reason != null && !reason.isBlank())
            c.setProjectDescription((c.getProjectDescription() != null ? c.getProjectDescription() + "\n" : "")
                    + "[CANCELLED] " + reason);
        return contractRepository.save(c);
    }

    // ── 4. Renew a contract ────────────────────────────────────────────────────
    public Contract renewContract(Integer id, ContractRenewalDTO dto) {
        Contract original = findOrThrow(id);
        if (original.getStatus() != ContractStatus.ACTIVE && original.getStatus() != ContractStatus.EXPIRED)
            throw new IllegalStateException("Only ACTIVE or EXPIRED contracts can be renewed.");
        if (dto.getNewEndDate() == null || !dto.getNewEndDate().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("New end date must be in the future.");

        // Mark original as RENEWED
        original.setStatus(ContractStatus.RENEWED);
        contractRepository.save(original);

        // New start = today (always valid for @PrePersist)
        LocalDate newStart = LocalDate.now();
        LocalDate newEnd = dto.getNewEndDate();

        // Ensure end is after start
        if (!newEnd.isAfter(newStart))
            throw new IllegalArgumentException("New end date must be after today.");

        // Build renewed contract — bypass @PrePersist by setting dates that pass validation
        Contract renewed = new Contract();
        renewed.setContractTitle(original.getContractTitle() + " (Renewed)");
        renewed.setFreelancerName(original.getFreelancerName());
        renewed.setFreelancerEmail(original.getFreelancerEmail());
        renewed.setClientName(original.getClientName());
        renewed.setClientEmail(original.getClientEmail());
        renewed.setProjectDescription(original.getProjectDescription() != null
                ? original.getProjectDescription() : "");
        renewed.setStartDate(newStart);
        renewed.setEndDate(newEnd);
        renewed.setEndDate(dto.getNewEndDate());
        renewed.setStatus(ContractStatus.ACTIVE);
        renewed.setContractAmount(dto.getNewAmount() != null ? dto.getNewAmount() : original.getContractAmount());
        renewed.setPaymentType(original.getPaymentType());
        renewed.setCurrency(original.getCurrency());
        renewed.setContractType(original.getContractType());
        renewed.setDeliverables(original.getDeliverables());
        renewed.setTermsAndConditions(original.getTermsAndConditions());
        renewed.setPaymentTermsDays(original.getPaymentTermsDays());
        renewed.setCreatedBy("RENEWAL_OF_" + id);
        if (dto.getRenewalNote() != null && !dto.getRenewalNote().isBlank())
            renewed.setProjectDescription(renewed.getProjectDescription() + "\n[RENEWAL NOTE] " + dto.getRenewalNote());

        return contractRepository.save(renewed);
    }

    // ── 5. Raise a dispute ─────────────────────────────────────────────────────
    public Contract raiseDispute(Integer id, ContractDisputeDTO dto) {
        Contract c = findOrThrow(id);
        if (c.getStatus() != ContractStatus.ACTIVE)
            throw new IllegalStateException("Disputes can only be raised on ACTIVE contracts.");
        c.setStatus(ContractStatus.DISPUTED);
        String note = String.format("\n[DISPUTE by %s] Reason: %s — %s",
                dto.getRaisedBy(), dto.getReason(), dto.getDescription());
        c.setProjectDescription((c.getProjectDescription() != null ? c.getProjectDescription() : "") + note);
        return contractRepository.save(c);
    }

    // ── 6. Resolve a dispute ───────────────────────────────────────────────────
    public Contract resolveDispute(Integer id, String resolution) {
        Contract c = findOrThrow(id);
        if (c.getStatus() != ContractStatus.DISPUTED)
            throw new IllegalStateException("Contract is not in DISPUTED state.");
        c.setStatus(ContractStatus.ACTIVE);
        c.setProjectDescription((c.getProjectDescription() != null ? c.getProjectDescription() : "")
                + "\n[DISPUTE RESOLVED] " + resolution);
        return contractRepository.save(c);
    }

    // ── 7. Auto-expire overdue contracts (called by scheduler) ────────────────
    public int autoExpireOverdueContracts() {
        List<Contract> overdue = contractRepository.findOverdueContracts(LocalDate.now());
        overdue.forEach(c -> c.setStatus(ContractStatus.EXPIRED));
        contractRepository.saveAll(overdue);
        return overdue.size();
    }

    // ── 8. Contract summary with business metrics ──────────────────────────────
    public ContractSummaryDTO getSummary(Integer id) {
        Contract c = findOrThrow(id);
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), c.getEndDate());
        boolean expiringSoon = daysRemaining >= 0 && daysRemaining <= 7 && c.getStatus() == ContractStatus.ACTIVE;
        boolean overdue = daysRemaining < 0 && c.getStatus() == ContractStatus.ACTIVE;
        return new ContractSummaryDTO(
                c.getIdContract(), c.getContractTitle(),
                c.getFreelancerName(), c.getClientName(),
                c.getStatus().name(), c.getContractAmount(),
                c.getCurrency(), daysRemaining, expiringSoon, overdue
        );
    }

    // ── 9. Revenue analytics per freelancer ───────────────────────────────────
    public Map<String, Object> getFreelancerAnalytics(String email) {
        List<Contract> contracts = contractRepository.findByFreelancerEmailIgnoreCase(email);
        long total = contracts.size();
        long active = contracts.stream().filter(c -> c.getStatus() == ContractStatus.ACTIVE).count();
        long completed = contracts.stream().filter(c -> c.getStatus() == ContractStatus.COMPLETED).count();
        long disputed = contracts.stream().filter(c -> c.getStatus() == ContractStatus.DISPUTED).count();
        BigDecimal totalRevenue = contracts.stream()
                .filter(c -> c.getStatus() == ContractStatus.COMPLETED || c.getStatus() == ContractStatus.ACTIVE)
                .map(Contract::getContractAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgAmount = total > 0
                ? totalRevenue.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("freelancerEmail", email);
        result.put("totalContracts", total);
        result.put("activeContracts", active);
        result.put("completedContracts", completed);
        result.put("disputedContracts", disputed);
        result.put("totalRevenue", totalRevenue);
        result.put("averageContractAmount", avgAmount);
        return result;
    }

    // ── 10. Client portfolio analytics ────────────────────────────────────────
    public Map<String, Object> getClientAnalytics(String email) {
        List<Contract> contracts = contractRepository.findByClientEmailIgnoreCase(email);
        long total = contracts.size();
        BigDecimal totalSpent = contracts.stream()
                .filter(c -> c.getStatus() == ContractStatus.COMPLETED)
                .map(Contract::getContractAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Long> byStatus = contracts.stream()
                .collect(Collectors.groupingBy(c -> c.getStatus().name(), Collectors.counting()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("clientEmail", email);
        result.put("totalContracts", total);
        result.put("totalSpent", totalSpent);
        result.put("contractsByStatus", byStatus);
        return result;
    }

    // ── 11. Duplicate contract ─────────────────────────────────────────────────
    public Contract duplicateContract(Integer id) {
        Contract original = findOrThrow(id);
        Contract copy = new Contract();
        copy.setContractTitle(original.getContractTitle() + " (Copy)");
        copy.setFreelancerName(original.getFreelancerName());
        copy.setFreelancerEmail(original.getFreelancerEmail());
        copy.setClientName(original.getClientName());
        copy.setClientEmail(original.getClientEmail());
        copy.setProjectDescription(original.getProjectDescription());
        copy.setStartDate(LocalDate.now());
        copy.setEndDate(LocalDate.now().plusDays(
                ChronoUnit.DAYS.between(original.getStartDate(), original.getEndDate())));
        copy.setStatus(ContractStatus.PENDING);
        copy.setContractAmount(original.getContractAmount());
        copy.setPaymentType(original.getPaymentType());
        copy.setCurrency(original.getCurrency());
        copy.setContractType(original.getContractType());
        copy.setDeliverables(original.getDeliverables());
        copy.setTermsAndConditions(original.getTermsAndConditions());
        copy.setPaymentTermsDays(original.getPaymentTermsDays());
        copy.setCreatedBy("COPY_OF_" + id);
        return contractRepository.save(copy);
    }

    // ── 12. Bulk status update ─────────────────────────────────────────────────
    public List<Contract> bulkUpdateStatus(List<Integer> ids, ContractStatus newStatus) {
        List<Contract> contracts = contractRepository.findAllById(ids);
        contracts.forEach(c -> c.setStatus(newStatus));
        return contractRepository.saveAll(contracts);
    }

    // ── Helper ─────────────────────────────────────────────────────────────────
    private Contract findOrThrow(Integer id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found: " + id));
    }
}
