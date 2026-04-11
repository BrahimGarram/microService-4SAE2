package com.example.freelanceplatformspringapp.Complaints.Entity;

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
public class Penalty {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPenalty;
    
    private Long userId;  // User who received the penalty
    
    private Long complaintId;  // Related complaint (optional)
    
    @Enumerated(EnumType.STRING)
    private PenaltyType penaltyType;
    
    @Enumerated(EnumType.STRING)
    private PenaltySeverity severity;
    
    private String reason;  // Why the penalty was applied
    
    private String description;  // Detailed description
    
    private LocalDateTime appliedAt;  // When penalty was applied
    
    private Date expiresAt;  // When penalty expires (null for permanent)
    
    private boolean isActive;  // Is penalty currently active
    
    private Long appliedByAdminId;  // Admin who applied the penalty
    
    private Double fineAmount;  // Amount if penalty type is FINE
    
    private String ruleName;  // Name of the rule that triggered this penalty
}
