package com.microservices.contract.repository;

import com.microservices.contract.entity.Payment;
import com.microservices.contract.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer>,
        JpaSpecificationExecutor<Payment> {

    // ── Basic derived queries ──────────────────────────────────────────────────

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByContractId(Integer contractId);

    List<Payment> findByContractIdAndStatus(Integer contractId, PaymentStatus status);

    List<Payment> findByPaymentMethodIgnoreCase(String method);

    // ── @Query: monthly revenue for a given year ───────────────────────────────

    @Query("SELECT MONTH(p.paymentDate), SUM(p.amount) FROM Payment p " +
           "WHERE YEAR(p.paymentDate) = :year AND p.status = 'COMPLETED' " +
           "GROUP BY MONTH(p.paymentDate) ORDER BY MONTH(p.paymentDate)")
    List<Object[]> getMonthlyRevenue(@Param("year") int year);

    // ── @Query: failed payments older than N days ──────────────────────────────

    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.paymentDate < :cutoff")
    List<Payment> findFailedPaymentsOlderThan(@Param("cutoff") LocalDate cutoff);

    // ── @Query: revenue by payment method ─────────────────────────────────────

    @Query("SELECT p.paymentMethod, SUM(p.amount) FROM Payment p " +
           "WHERE p.status = 'COMPLETED' GROUP BY p.paymentMethod")
    List<Object[]> sumAmountByPaymentMethod();

    // ── @Query: duplicate transaction IDs (fraud detection) ───────────────────

    @Query("SELECT p.transactionId, COUNT(p) FROM Payment p " +
           "WHERE p.transactionId IS NOT NULL " +
           "GROUP BY p.transactionId HAVING COUNT(p) > 1")
    List<Object[]> findDuplicateTransactions();

    // ── @Query: average payment amount ────────────────────────────────────────

    @Query("SELECT AVG(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal getAveragePaymentAmount();

    // ── @Query: total revenue ──────────────────────────────────────────────────

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'")
    BigDecimal getTotalRevenue();

    // ── @Query: payments in date range ────────────────────────────────────────

    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :from AND :to ORDER BY p.paymentDate DESC")
    List<Payment> findByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    // ── @Query: pending payments count per contract ────────────────────────────

    @Query("SELECT p.contractId, COUNT(p) FROM Payment p " +
           "WHERE p.status = 'PENDING' GROUP BY p.contractId")
    List<Object[]> countPendingPaymentsPerContract();

    // ── @Query: count by status ────────────────────────────────────────────────

    @Query("SELECT p.status, COUNT(p) FROM Payment p GROUP BY p.status")
    List<Object[]> countByStatusGrouped();
}
