package com.microservices.contract.repository;

import com.microservices.contract.entity.Contract;
import com.microservices.contract.entity.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Integer>,
        JpaSpecificationExecutor<Contract> {

    // ── Basic derived queries ──────────────────────────────────────────────────

    List<Contract> findByStatus(ContractStatus status);

    List<Contract> findByFreelancerEmailIgnoreCase(String email);

    List<Contract> findByClientEmailIgnoreCase(String email);

    List<Contract> findByContractAmountBetween(BigDecimal min, BigDecimal max);

    List<Contract> findByFreelancerEmailIgnoreCaseAndStatus(String email, ContractStatus status);

    // ── @Query: expiring soon ──────────────────────────────────────────────────

    @Query("SELECT c FROM Contract c WHERE c.endDate BETWEEN :today AND :limit AND c.status = 'ACTIVE'")
    List<Contract> findExpiringSoon(@Param("today") LocalDate today, @Param("limit") LocalDate limit);

    // ── @Query: overdue (endDate passed but still ACTIVE) ─────────────────────

    @Query("SELECT c FROM Contract c WHERE c.endDate < :today AND c.status = 'ACTIVE'")
    List<Contract> findOverdueContracts(@Param("today") LocalDate today);

    // ── @Query: full-text keyword search ──────────────────────────────────────

    @Query("SELECT c FROM Contract c WHERE " +
           "LOWER(c.contractTitle) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(c.projectDescription) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(c.deliverables) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(c.freelancerName) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
           "LOWER(c.clientName) LIKE LOWER(CONCAT('%',:kw,'%'))")
    List<Contract> searchFullText(@Param("kw") String keyword);

    // ── @Query: count by status ────────────────────────────────────────────────

    @Query("SELECT c.status, COUNT(c) FROM Contract c GROUP BY c.status")
    List<Object[]> countByStatusGrouped();

    // ── @Query: top earning freelancers ───────────────────────────────────────

    @Query("SELECT c.freelancerEmail, SUM(c.contractAmount) FROM Contract c " +
           "WHERE c.status = 'ACTIVE' GROUP BY c.freelancerEmail ORDER BY SUM(c.contractAmount) DESC")
    List<Object[]> findTopEarningFreelancers();

    // ── @Query: client history sorted by amount ───────────────────────────────

    @Query("SELECT c FROM Contract c WHERE LOWER(c.clientEmail) = LOWER(:email) ORDER BY c.contractAmount DESC")
    List<Contract> findByClientEmailOrderByAmountDesc(@Param("email") String email);
}
