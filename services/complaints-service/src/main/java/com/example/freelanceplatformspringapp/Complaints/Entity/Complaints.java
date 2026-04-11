package com.example.freelanceplatformspringapp.Complaints.Entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class Complaints {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReclamation;
    private Long userId;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    @Nullable
    private Date updatedAt;
    @Enumerated(EnumType.STRING)
    private ClaimStatus claimStatus;
    @Enumerated(EnumType.STRING)
    private ClaimPriority claimPriority;
    @Enumerated(EnumType.STRING)
    private ClaimCategory category;
    private Long assignedToAdminId;
    private String resolutionNote;
    @Nullable
    private Date resolvedAt;
    @Nullable
    private Date lastReminderSentAt;
    private boolean isVisible;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "claim_attachment_id")
    private ClaimAttachment claimAttachment;

    public Complaints() {
    }

    public Long getIdReclamation() {
        return idReclamation;
    }

    public void setIdReclamation(Long idReclamation) {
        this.idReclamation = idReclamation;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ClaimStatus getClaimStatus() {
        return claimStatus;
    }

    public void setClaimStatus(ClaimStatus claimStatus) {
        this.claimStatus = claimStatus;
    }

    public ClaimPriority getClaimPriority() {
        return claimPriority;
    }

    public void setClaimPriority(ClaimPriority claimPriority) {
        this.claimPriority = claimPriority;
    }

    public ClaimCategory getCategory() {
        return category;
    }

    public void setCategory(ClaimCategory category) {
        this.category = category;
    }

    public Long getAssignedToAdminId() {
        return assignedToAdminId;
    }

    public void setAssignedToAdminId(Long assignedToAdminId) {
        this.assignedToAdminId = assignedToAdminId;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }

    public Date getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Date getLastReminderSentAt() {
        return lastReminderSentAt;
    }

    public void setLastReminderSentAt(Date lastReminderSentAt) {
        this.lastReminderSentAt = lastReminderSentAt;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public ClaimAttachment getClaimAttachment() {
        return claimAttachment;
    }

    public void setClaimAttachment(ClaimAttachment claimAttachment) {
        this.claimAttachment = claimAttachment;
    }
}
