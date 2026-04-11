package com.microservices.contract.controller;

import com.microservices.contract.dto.*;
import com.microservices.contract.entity.Contract;
import com.microservices.contract.entity.ContractStatus;
import com.microservices.contract.service.ContractMetierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractMetierController {

    private final ContractMetierService metierService;

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Integer id) {
        try { return ResponseEntity.ok(metierService.activateContract(id)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Integer id) {
        try { return ResponseEntity.ok(metierService.completeContract(id)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Integer id,
                                    @RequestParam(required = false) String reason) {
        try { return ResponseEntity.ok(metierService.cancelContract(id, reason)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<?> renew(@PathVariable Integer id,
                                   @RequestBody ContractRenewalDTO dto) {
        try { return ResponseEntity.ok(metierService.renewContract(id, dto)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    // ── Dispute ────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/dispute")
    public ResponseEntity<?> raiseDispute(@PathVariable Integer id,
                                          @RequestBody ContractDisputeDTO dto) {
        try { return ResponseEntity.ok(metierService.raiseDispute(id, dto)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PatchMapping("/{id}/dispute/resolve")
    public ResponseEntity<?> resolveDispute(@PathVariable Integer id,
                                            @RequestParam String resolution) {
        try { return ResponseEntity.ok(metierService.resolveDispute(id, resolution)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    // ── Utilities ──────────────────────────────────────────────────────────────

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<?> duplicate(@PathVariable Integer id) {
        try { return ResponseEntity.ok(metierService.duplicateContract(id)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/bulk-status")
    public ResponseEntity<List<Contract>> bulkStatus(@RequestParam ContractStatus status,
                                                     @RequestBody List<Integer> ids) {
        return ResponseEntity.ok(metierService.bulkUpdateStatus(ids, status));
    }

    @PostMapping("/auto-expire")
    public ResponseEntity<String> autoExpire() {
        int count = metierService.autoExpireOverdueContracts();
        return ResponseEntity.ok(count + " contracts expired.");
    }

    // ── Analytics ──────────────────────────────────────────────────────────────

    @GetMapping("/{id}/summary")
    public ResponseEntity<ContractSummaryDTO> summary(@PathVariable Integer id) {
        return ResponseEntity.ok(metierService.getSummary(id));
    }

    @GetMapping("/analytics/freelancer/{email}")
    public ResponseEntity<Map<String, Object>> freelancerAnalytics(@PathVariable String email) {
        return ResponseEntity.ok(metierService.getFreelancerAnalytics(email));
    }

    @GetMapping("/analytics/client/{email}")
    public ResponseEntity<Map<String, Object>> clientAnalytics(@PathVariable String email) {
        return ResponseEntity.ok(metierService.getClientAnalytics(email));
    }
}
