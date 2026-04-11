package com.microservices.contract.controller;

import com.microservices.contract.dto.*;
import com.microservices.contract.entity.Payment;
import com.microservices.contract.entity.PaymentStatus;
import com.microservices.contract.service.PaymentService;
import com.microservices.contract.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final StripeService stripeService;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<Payment>> getAll() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getById(@PathVariable Integer id) {
        return paymentService.getPaymentById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<Payment>> getByContract(@PathVariable Integer contractId) {
        return ResponseEntity.ok(paymentService.getPaymentsByContract(contractId));
    }

    @PostMapping
    public ResponseEntity<Payment> create(@RequestBody Payment payment) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(payment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> update(@PathVariable Integer id, @RequestBody Payment payment) {
        return ResponseEntity.ok(paymentService.updatePayment(id, payment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    // ── Advanced endpoints ────────────────────────────────────────────────────

    @GetMapping("/revenue/monthly")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlyRevenue(
            @RequestParam(defaultValue = "2026") int year) {
        return ResponseEntity.ok(paymentService.getMonthlyRevenue(year));
    }

    @GetMapping("/failed/old")
    public ResponseEntity<List<Payment>> getFailedOld(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(paymentService.getFailedPaymentsOlderThan(days));
    }

    @GetMapping("/revenue/by-method")
    public ResponseEntity<Map<String, BigDecimal>> getRevenueByMethod() {
        return ResponseEntity.ok(paymentService.getRevenueByMethod());
    }

    @GetMapping("/duplicates")
    public ResponseEntity<List<Object[]>> getDuplicates() {
        return ResponseEntity.ok(paymentService.getDuplicateTransactions());
    }

    @GetMapping("/average")
    public ResponseEntity<BigDecimal> getAverage() {
        return ResponseEntity.ok(paymentService.getAveragePaymentAmount());
    }

    @GetMapping("/total-revenue")
    public ResponseEntity<BigDecimal> getTotalRevenue() {
        return ResponseEntity.ok(paymentService.getTotalRevenue());
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Payment>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(paymentService.getPaymentsByDateRange(from, to));
    }

    @GetMapping("/pending-per-contract")
    public ResponseEntity<Map<String, Long>> getPendingPerContract() {
        return ResponseEntity.ok(paymentService.getPendingPaymentsPerContract());
    }

    @PostMapping("/filter")
    public ResponseEntity<List<Payment>> filter(@RequestBody PaymentFilterDTO filterDTO) {
        return ResponseEntity.ok(paymentService.searchWithFilters(filterDTO));
    }

    @GetMapping("/stats")
    public ResponseEntity<PaymentStatsDTO> getStats(
            @RequestParam(defaultValue = "2026") int year) {
        return ResponseEntity.ok(paymentService.getStats(year));
    }

    // ── Stripe endpoints ──────────────────────────────────────────────────────

    @PostMapping("/stripe/create-intent")
    public ResponseEntity<?> createStripeIntent(@RequestBody StripePaymentRequestDTO request) {
        if (request.getContractId() == null) {
            return ResponseEntity.badRequest().body("contractId is required");
        }
        try {
            return ResponseEntity.ok(stripeService.createPaymentIntent(request));
        } catch (Exception e) {
            System.err.println("[Stripe ERROR] " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/stripe/confirm/{paymentIntentId}")
    public ResponseEntity<?> confirmStripePayment(@PathVariable String paymentIntentId) {
        try {
            return ResponseEntity.ok(stripeService.confirmPayment(paymentIntentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
