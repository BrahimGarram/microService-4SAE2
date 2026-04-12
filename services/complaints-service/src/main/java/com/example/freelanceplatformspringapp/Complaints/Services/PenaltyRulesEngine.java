package com.example.freelanceplatformspringapp.Complaints.Services;

import com.example.freelanceplatformspringapp.Complaints.Entity.*;
import com.example.freelanceplatformspringapp.Complaints.Repository.ComplaintsRepository;
import com.example.freelanceplatformspringapp.Complaints.Repository.PenaltyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class PenaltyRulesEngine {

    private final PenaltyRepository penaltyRepository;
    private final ComplaintsRepository complaintsRepository;
    private final EmailService emailService;

    /**
     * Evaluate all rules for a user and apply penalties if needed
     */
    public List<Penalty> evaluateRulesForUser(Long userId) {
        log.info("Evaluating penalty rules for user ID: {}", userId);
        
        List<Penalty> appliedPenalties = new ArrayList<>();
        
        // Rule 1: Multiple Urgent Complaints
        Penalty urgentPenalty = checkMultipleUrgentComplaints(userId);
        if (urgentPenalty != null) {
            appliedPenalties.add(urgentPenalty);
        }
        
        // Rule 2: Repeated Complaints Same Category
        Penalty repeatedCategoryPenalty = checkRepeatedComplaintsSameCategory(userId);
        if (repeatedCategoryPenalty != null) {
            appliedPenalties.add(repeatedCategoryPenalty);
        }
        
        // Rule 3: Excessive Complaint Frequency
        Penalty frequencyPenalty = checkExcessiveComplaintFrequency(userId);
        if (frequencyPenalty != null) {
            appliedPenalties.add(frequencyPenalty);
        }
        
        // Rule 4: Rejected Complaints Pattern
        Penalty rejectedPenalty = checkRejectedComplaintsPattern(userId);
        if (rejectedPenalty != null) {
            appliedPenalties.add(rejectedPenalty);
        }
        
        log.info("Applied {} penalties for user ID: {}", appliedPenalties.size(), userId);
        return appliedPenalties;
    }

    /**
     * Rule 1: Multiple Urgent Complaints in Short Time
     * Complaints 1-2: No penalty (free)
     * Complaint 3: WARNING
     * Complaint 4+: ACCOUNT_RESTRICTION
     */
    private Penalty checkMultipleUrgentComplaints(Long userId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        
        List<Complaints> allComplaints = complaintsRepository.findByUserIdAndIsVisibleTrue(userId);
        long urgentCount = allComplaints.stream()
            .filter(c -> c.getClaimPriority() == ClaimPriority.Urgent)
            .filter(c -> c.getCreatedAt().isAfter(sevenDaysAgo))
            .count();
        
        // Count existing warnings for this rule
        long existingWarnings = penaltyRepository.findByUserIdAndIsActiveTrue(userId).stream()
            .filter(p -> "MULTIPLE_URGENT_COMPLAINTS".equals(p.getRuleName()))
            .filter(p -> p.getPenaltyType() == PenaltyType.WARNING)
            .count();
        
        if (urgentCount >= 4 && existingWarnings >= 1) {
            // After warning, escalate to ACCOUNT_RESTRICTION
            log.warn("User {} has {} urgent complaints - escalating to ACCOUNT_RESTRICTION", userId, urgentCount);
            
            Penalty penalty = new Penalty();
            penalty.setUserId(userId);
            penalty.setPenaltyType(PenaltyType.ACCOUNT_RESTRICTION);
            penalty.setSeverity(PenaltySeverity.MEDIUM);
            penalty.setReason("Excessive Urgent Complaints");
            penalty.setDescription(String.format("User created %d urgent complaints in the last 7 days after receiving a warning. Account restricted for 7 days.", urgentCount));
            penalty.setAppliedAt(LocalDateTime.now());
            penalty.setExpiresAt(Date.from(LocalDateTime.now().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()));
            penalty.setActive(true);
            penalty.setRuleName("MULTIPLE_URGENT_COMPLAINTS");
            
            return penaltyRepository.save(penalty);
        } else if (urgentCount == 3 && existingWarnings < 1) {
            // 3rd complaint = warning
            log.warn("User {} has {} urgent complaints - issuing warning", userId, urgentCount);
            
            Penalty penalty = new Penalty();
            penalty.setUserId(userId);
            penalty.setPenaltyType(PenaltyType.WARNING);
            penalty.setSeverity(PenaltySeverity.LOW);
            penalty.setReason("Multiple Urgent Complaints - Warning");
            penalty.setDescription(String.format("User created %d urgent complaints in the last 7 days. Please use urgent priority only for critical issues. Next violation will result in account restriction.", urgentCount));
            penalty.setAppliedAt(LocalDateTime.now());
            penalty.setActive(true);
            penalty.setRuleName("MULTIPLE_URGENT_COMPLAINTS");
            
            return penaltyRepository.save(penalty);
        }
        
        return null;
    }

    /**
     * Rule 2: Repeated Complaints in Same Category
     * Complaints 1-2: No penalty (free)
     * Complaint 3: WARNING
     * Complaint 4+: ACCOUNT_RESTRICTION
     */
    private Penalty checkRepeatedComplaintsSameCategory(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        List<Complaints> allComplaints = complaintsRepository.findByUserIdAndIsVisibleTrue(userId);
        
        // Count existing warnings for this rule
        long existingWarnings = penaltyRepository.findByUserIdAndIsActiveTrue(userId).stream()
            .filter(p -> "REPEATED_CATEGORY_COMPLAINTS".equals(p.getRuleName()))
            .filter(p -> p.getPenaltyType() == PenaltyType.WARNING)
            .count();
        
        // Group by category and count
        for (ClaimCategory category : ClaimCategory.values()) {
            long categoryCount = allComplaints.stream()
                .filter(c -> c.getCategory() == category)
                .filter(c -> c.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();
            
            if (categoryCount >= 4 && existingWarnings >= 1) {
                // Escalate to ACCOUNT_RESTRICTION after warning
                log.warn("User {} has {} complaints in category {} - escalating to RESTRICTION", userId, categoryCount, category);
                
                Penalty penalty = new Penalty();
                penalty.setUserId(userId);
                penalty.setPenaltyType(PenaltyType.ACCOUNT_RESTRICTION);
                penalty.setSeverity(PenaltySeverity.MEDIUM);
                penalty.setReason("Repeated Complaints - Same Category");
                penalty.setDescription(String.format("User created %d complaints in %s category in the last 30 days after receiving a warning. Account restricted for 7 days.", categoryCount, category));
                penalty.setAppliedAt(LocalDateTime.now());
                penalty.setExpiresAt(Date.from(LocalDateTime.now().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()));
                penalty.setActive(true);
                penalty.setRuleName("REPEATED_CATEGORY_COMPLAINTS");
                
                return penaltyRepository.save(penalty);
            } else if (categoryCount == 3 && existingWarnings < 1) {
                // 3rd complaint = warning
                log.warn("User {} has {} complaints in category {} - issuing warning", userId, categoryCount, category);
                
                Penalty penalty = new Penalty();
                penalty.setUserId(userId);
                penalty.setPenaltyType(PenaltyType.WARNING);
                penalty.setSeverity(PenaltySeverity.LOW);
                penalty.setReason(String.format("Repeated Complaints in %s - Warning", category));
                penalty.setDescription(String.format("User created %d complaints in %s category in the last 30 days. Please avoid duplicate complaints. Next violation will result in account restriction.", categoryCount, category));
                penalty.setAppliedAt(LocalDateTime.now());
                penalty.setActive(true);
                penalty.setRuleName("REPEATED_CATEGORY_COMPLAINTS");
                
                return penaltyRepository.save(penalty);
            }
        }
        
        return null;
    }

    /**
     * Rule 3: Excessive Complaint Frequency
     * Complaints 1-2: No penalty (free)
     * Complaint 3: WARNING
     * Complaint 4+: ACCOUNT_RESTRICTION
     */
    private Penalty checkExcessiveComplaintFrequency(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        List<Complaints> allComplaints = complaintsRepository.findByUserIdAndIsVisibleTrue(userId);
        long recentCount = allComplaints.stream()
            .filter(c -> c.getCreatedAt().isAfter(thirtyDaysAgo))
            .count();
        
        // Count existing warnings for this rule
        long existingWarnings = penaltyRepository.findByUserIdAndIsActiveTrue(userId).stream()
            .filter(p -> "EXCESSIVE_FREQUENCY".equals(p.getRuleName()))
            .filter(p -> p.getPenaltyType() == PenaltyType.WARNING)
            .count();
        
        if (recentCount >= 4 && existingWarnings >= 1) {
            // Escalate to ACCOUNT_RESTRICTION after warning
            log.warn("User {} has {} complaints in last 30 days - escalating to RESTRICTION", userId, recentCount);
            
            Penalty penalty = new Penalty();
            penalty.setUserId(userId);
            penalty.setPenaltyType(PenaltyType.ACCOUNT_RESTRICTION);
            penalty.setSeverity(PenaltySeverity.MEDIUM);
            penalty.setReason("High Complaint Frequency");
            penalty.setDescription(String.format("User created %d complaints in the last 30 days after receiving a warning. Account restricted for 7 days.", recentCount));
            penalty.setAppliedAt(LocalDateTime.now());
            penalty.setExpiresAt(Date.from(LocalDateTime.now().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()));
            penalty.setActive(true);
            penalty.setRuleName("EXCESSIVE_FREQUENCY");
            
            return penaltyRepository.save(penalty);
        } else if (recentCount == 3 && existingWarnings < 1) {
            // 3rd complaint = warning
            log.warn("User {} has {} complaints in last 30 days - issuing warning", userId, recentCount);
            
            Penalty penalty = new Penalty();
            penalty.setUserId(userId);
            penalty.setPenaltyType(PenaltyType.WARNING);
            penalty.setSeverity(PenaltySeverity.LOW);
            penalty.setReason("High Complaint Frequency - Warning");
            penalty.setDescription(String.format("User created %d complaints in the last 30 days. Please ensure complaints are valid. Next violation will result in account restriction.", recentCount));
            penalty.setAppliedAt(LocalDateTime.now());
            penalty.setActive(true);
            penalty.setRuleName("EXCESSIVE_FREQUENCY");
            
            return penaltyRepository.save(penalty);
        }
        
        return null;
    }

    /**
     * Rule 4: Pattern of Rejected Complaints
     * Rejections 1-2: No penalty (free)
     * Rejection 3: WARNING
     * Rejection 4+: ACCOUNT_RESTRICTION
     */
    private Penalty checkRejectedComplaintsPattern(Long userId) {
        LocalDateTime sixtyDaysAgo = LocalDateTime.now().minusDays(60);
        
        List<Complaints> allComplaints = complaintsRepository.findByUserIdAndIsVisibleTrue(userId);
        long rejectedCount = allComplaints.stream()
            .filter(c -> c.getClaimStatus() == ClaimStatus.Rejected)
            .filter(c -> c.getCreatedAt().isAfter(sixtyDaysAgo))
            .count();
        
        // Count existing warnings for this rule
        long existingWarnings = penaltyRepository.findByUserIdAndIsActiveTrue(userId).stream()
            .filter(p -> "REJECTED_COMPLAINTS_PATTERN".equals(p.getRuleName()))
            .filter(p -> p.getPenaltyType() == PenaltyType.WARNING)
            .count();
        
        if (rejectedCount >= 4 && existingWarnings >= 1) {
            // Escalate to ACCOUNT_RESTRICTION after warning
            log.warn("User {} has {} rejected complaints - escalating to RESTRICTION", userId, rejectedCount);
            
            Penalty penalty = new Penalty();
            penalty.setUserId(userId);
            penalty.setPenaltyType(PenaltyType.ACCOUNT_RESTRICTION);
            penalty.setSeverity(PenaltySeverity.MEDIUM);
            penalty.setReason("Excessive Invalid Complaints");
            penalty.setDescription(String.format("User has %d rejected complaints in the last 60 days after receiving a warning. Account restricted for 7 days.", rejectedCount));
            penalty.setAppliedAt(LocalDateTime.now());
            penalty.setExpiresAt(Date.from(LocalDateTime.now().plusDays(7).atZone(ZoneId.systemDefault()).toInstant()));
            penalty.setActive(true);
            penalty.setRuleName("REJECTED_COMPLAINTS_PATTERN");
            
            return penaltyRepository.save(penalty);
        } else if (rejectedCount == 3 && existingWarnings < 1) {
            // 3rd rejection = warning
            log.warn("User {} has {} rejected complaints - issuing warning", userId, rejectedCount);
            
            Penalty penalty = new Penalty();
            penalty.setUserId(userId);
            penalty.setPenaltyType(PenaltyType.WARNING);
            penalty.setSeverity(PenaltySeverity.LOW);
            penalty.setReason("Invalid Complaints - Warning");
            penalty.setDescription(String.format("User has %d rejected complaints in the last 60 days. Please ensure complaints are valid before submitting. Next violation will result in account restriction.", rejectedCount));
            penalty.setAppliedAt(LocalDateTime.now());
            penalty.setActive(true);
            penalty.setRuleName("REJECTED_COMPLAINTS_PATTERN");
            
            return penaltyRepository.save(penalty);
        }
        
        return null;
    }

    /**
     * Manually apply a penalty by admin
     */
    public Penalty applyManualPenalty(Long userId, Long complaintId, PenaltyType type, 
                                     PenaltySeverity severity, String reason, 
                                     String description, Long adminId, Integer daysToExpire) {
        log.info("Admin {} applying manual penalty to user {}", adminId, userId);
        
        Penalty penalty = new Penalty();
        penalty.setUserId(userId);
        penalty.setComplaintId(complaintId);
        penalty.setPenaltyType(type);
        penalty.setSeverity(severity);
        penalty.setReason(reason);
        penalty.setDescription(description);
        penalty.setAppliedAt(LocalDateTime.now());
        penalty.setAppliedByAdminId(adminId);
        penalty.setActive(true);
        penalty.setRuleName("MANUAL_PENALTY");
        
        if (daysToExpire != null && daysToExpire > 0) {
            penalty.setExpiresAt(Date.from(
                LocalDateTime.now().plusDays(daysToExpire)
                    .atZone(ZoneId.systemDefault()).toInstant()
            ));
        }
        
        Penalty saved = penaltyRepository.save(penalty);
        
        // Send notification to user
        try {
            String userEmail = getUserEmail(userId);
            if (userEmail != null) {
                sendPenaltyNotification(userEmail, saved);
            }
        } catch (Exception e) {
            log.error("Failed to send penalty notification", e);
        }
        
        return saved;
    }

    /**
     * Get all active penalties for a user
     */
    public List<Penalty> getActivePenaltiesForUser(Long userId) {
        return penaltyRepository.findByUserIdAndIsActiveTrue(userId);
    }

    /**
     * Check if user has any active penalties
     */
    public boolean hasActivePenalties(Long userId) {
        return penaltyRepository.countByUserIdAndIsActiveTrue(userId) > 0;
    }

    /**
     * Deactivate expired penalties
     */
    public void deactivateExpiredPenalties() {
        List<Penalty> allPenalties = penaltyRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        
        for (Penalty penalty : allPenalties) {
            if (penalty.isActive() && penalty.getExpiresAt() != null) {
                LocalDateTime expiryDate = penalty.getExpiresAt()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
                
                if (expiryDate.isBefore(now)) {
                    penalty.setActive(false);
                    penaltyRepository.save(penalty);
                    log.info("Deactivated expired penalty ID: {}", penalty.getIdPenalty());
                }
            }
        }
    }

    /**
     * Manually deactivate a penalty (admin action)
     */
    public Penalty deactivatePenalty(Long penaltyId) {
        Penalty penalty = penaltyRepository.findById(penaltyId)
            .orElseThrow(() -> new RuntimeException("Penalty not found with ID: " + penaltyId));
        
        if (!penalty.isActive()) {
            log.warn("Penalty ID {} is already inactive", penaltyId);
            return penalty;
        }
        
        penalty.setActive(false);
        Penalty saved = penaltyRepository.save(penalty);
        log.info("Admin manually deactivated penalty ID: {}", penaltyId);
        
        return saved;
    }

    /**
     * Send penalty notification email
     */
    private void sendPenaltyNotification(String userEmail, Penalty penalty) {
        // This would integrate with your EmailService
        log.info("Sending penalty notification to: {}", userEmail);
        // TODO: Implement email template for penalty notifications
    }

    /**
     * Fetch user email (placeholder - integrate with your user service)
     */
    private String getUserEmail(Long userId) {
        // TODO: Integrate with user service
        return null;
    }
}
