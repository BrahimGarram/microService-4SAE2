package com.microservices.contract.controller;

import com.microservices.contract.dto.ContractFilterDTO;
import com.microservices.contract.dto.ContractStatsDTO;
import com.microservices.contract.entity.Contract;
import com.microservices.contract.entity.ContractStatus;
import com.microservices.contract.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<Contract>> getAll() {
        return ResponseEntity.ok(contractService.getAllContracts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contract> getById(@PathVariable Integer id) {
        return contractService.getContractById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Contract>> getByStatus(@PathVariable ContractStatus status) {
        return ResponseEntity.ok(contractService.getContractsByStatus(status));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Contract contract) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(contractService.createContract(contract));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Contract contract) {
        try {
            return ResponseEntity.ok(contractService.updateContract(id, contract));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        contractService.deleteContract(id);
        return ResponseEntity.noContent().build();
    }

    // ── Advanced endpoints ────────────────────────────────────────────────────

    @GetMapping("/expiring")
    public ResponseEntity<List<Contract>> getExpiringSoon(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(contractService.getExpiringSoon(days));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Contract>> getOverdue() {
        return ResponseEntity.ok(contractService.getOverdueContracts());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Contract>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(contractService.searchFullText(keyword));
    }

    @GetMapping("/amount-range")
    public ResponseEntity<List<Contract>> getByAmountRange(
            @RequestParam BigDecimal min, @RequestParam BigDecimal max) {
        return ResponseEntity.ok(contractService.getByAmountRange(min, max));
    }

    @GetMapping("/freelancer/{email}/status/{status}")
    public ResponseEntity<List<Contract>> getByFreelancerAndStatus(
            @PathVariable String email, @PathVariable ContractStatus status) {
        return ResponseEntity.ok(contractService.getByFreelancerAndStatus(email, status));
    }

    @GetMapping("/client/{email}/history")
    public ResponseEntity<List<Contract>> getClientHistory(@PathVariable String email) {
        return ResponseEntity.ok(contractService.getClientHistoryByAmount(email));
    }

    @GetMapping("/top-freelancers")
    public ResponseEntity<Map<String, BigDecimal>> getTopFreelancers() {
        return ResponseEntity.ok(contractService.getTopEarningFreelancers());
    }

    @PostMapping("/filter")
    public ResponseEntity<List<Contract>> filter(@RequestBody ContractFilterDTO filterDTO) {
        return ResponseEntity.ok(contractService.searchWithFilters(filterDTO));
    }

    @GetMapping("/stats")
    public ResponseEntity<ContractStatsDTO> getStats() {
        return ResponseEntity.ok(contractService.getStats());
    }
}
