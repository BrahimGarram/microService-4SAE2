package com.microservices.contract.service;

import com.microservices.contract.dto.PaymentFilterDTO;
import com.microservices.contract.dto.PaymentStatsDTO;
import com.microservices.contract.entity.Payment;
import com.microservices.contract.entity.PaymentStatus;
import com.microservices.contract.repository.PaymentRepository;
import com.microservices.contract.specification.PaymentSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public List<Payment> getAllPayments() { return paymentRepository.findAll(); }
    public Optional<Payment> getPaymentById(Integer id) { return paymentRepository.findById(id); }
    public List<Payment> getPaymentsByStatus(PaymentStatus status) { return paymentRepository.findByStatus(status); }
    public List<Payment> getPaymentsByContract(Integer contractId) { return paymentRepository.findByContractId(contractId); }

    public Payment createPayment(Payment payment) { return paymentRepository.save(payment); }

    public Payment updatePayment(Integer id, Payment payment) {
        payment.setIdPayment(id);
        return paymentRepository.save(payment);
    }

    public void deletePayment(Integer id) { paymentRepository.deleteById(id); }

    // Advanced queries
    public Map<String, BigDecimal> getMonthlyRevenue(int year) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        paymentRepository.getMonthlyRevenue(year)
                .forEach(r -> result.put(months[(int)r[0] - 1], (BigDecimal) r[1]));
        return result;
    }

    public List<Payment> getFailedPaymentsOlderThan(int days) {
        return paymentRepository.findFailedPaymentsOlderThan(LocalDate.now().minusDays(days));
    }

    public Map<String, BigDecimal> getRevenueByMethod() {
        return paymentRepository.sumAmountByPaymentMethod().stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (BigDecimal) r[1]));
    }

    public List<Object[]> getDuplicateTransactions() {
        return paymentRepository.findDuplicateTransactions();
    }

    public BigDecimal getAveragePaymentAmount() {
        BigDecimal avg = paymentRepository.getAveragePaymentAmount();
        return avg != null ? avg : BigDecimal.ZERO;
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal total = paymentRepository.getTotalRevenue();
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<Payment> getPaymentsByDateRange(LocalDate from, LocalDate to) {
        return paymentRepository.findByDateRange(from, to);
    }

    public Map<String, Long> getPendingPaymentsPerContract() {
        return paymentRepository.countPendingPaymentsPerContract().stream()
                .collect(Collectors.toMap(r -> r[0].toString(), r -> (Long) r[1]));
    }

    public List<Payment> searchWithFilters(PaymentFilterDTO filter) {
        return paymentRepository.findAll(PaymentSpecification.withFilters(filter));
    }

    public PaymentStatsDTO getStats(int year) {
        long total = paymentRepository.count();
        BigDecimal revenue = getTotalRevenue();
        BigDecimal avg = getAveragePaymentAmount();
        Map<String, Long> byStatus = paymentRepository.countByStatusGrouped().stream()
                .collect(Collectors.toMap(r -> r[0].toString(), r -> (Long) r[1]));
        Map<String, BigDecimal> byMethod = getRevenueByMethod();
        Map<String, BigDecimal> byMonth = getMonthlyRevenue(year);
        long failedOld = getFailedPaymentsOlderThan(7).size();
        long duplicates = getDuplicateTransactions().size();
        return new PaymentStatsDTO(total, revenue, avg, byStatus, byMethod, byMonth, failedOld, duplicates);
    }
}
