package com.example.freelanceplatformspringapp.Complaints.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPenalty;
    private Long userId;
    private Long complaintId;
    @Enumerated(EnumType.STRING)
    private PenaltyType penaltyType;
    @Enumerated(EnumType.STRING)
    private PenaltySeverity severity;
    private String reason;
    private String description;
    private LocalDateTime appliedAt;
    private Date expiresAt;
    private boolean isActive;
    private Long appliedByAdminId;
    private Double fineAmount;
    private String ruleName;

    public Long getIdPenalty() {
        return idPenalty;
    }

    public void setIdPenalty(Long idPenalty) {
        this.idPenalty = idPenalty;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public PenaltyType getPenaltyType() {
        return penaltyType;
    }

    public void setPenaltyType(PenaltyType penaltyType) {
        this.penaltyType = penaltyType;
    }

    public PenaltySeverity getSeverity() {
        return severity;
    }

    public void setSeverity(PenaltySeverity severity) {
        this.severity = severity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Long getAppliedByAdminId() {
        return appliedByAdminId;
    }

    public void setAppliedByAdminId(Long appliedByAdminId) {
        this.appliedByAdminId = appliedByAdminId;
    }

    public Double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(Double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
}
