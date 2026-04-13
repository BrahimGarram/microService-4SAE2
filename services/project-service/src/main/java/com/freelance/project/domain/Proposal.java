package com.freelance.project.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "proj_proposals")
@Getter
@Setter
@NoArgsConstructor
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;
    private Long freelancerId;

    private BigDecimal amount;
    private Instant proposedDeadline;

    @Column(length = 4000)
    private String message;

    @Enumerated(EnumType.STRING)
    private ProposalStatus status = ProposalStatus.PENDING;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }
}
