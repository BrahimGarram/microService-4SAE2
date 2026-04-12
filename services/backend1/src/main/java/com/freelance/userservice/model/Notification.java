package com.freelance.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * In-app notification (e.g. "Nouvelle proposition", "Proposition acceptée").
 */
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** User who receives this notification */
    @Column(nullable = false)
    private Long targetUserId;

    /** User who triggered the notification (e.g. freelancer who submitted proposal) */
    private Long senderUserId;

    /** Type: NEW_PROPOSAL, PROPOSAL_ACCEPTED, PROPOSAL_REJECTED, etc. */
    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 500)
    private String message;

    /** Related entity ID (e.g. proposalId or projectId) for deep linking */
    private Long relatedId;

    @Column(name = "is_read", nullable = false)
    private Boolean read = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (read == null) read = false;
    }
}
