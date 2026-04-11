package com.example.freelanceplatformspringapp.Complaints.Entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Complaints {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReclamation;
    /**
     * Identifier of the user who created this complaint.
     * This should correspond to the user ID from the user-service.
     */
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
}
