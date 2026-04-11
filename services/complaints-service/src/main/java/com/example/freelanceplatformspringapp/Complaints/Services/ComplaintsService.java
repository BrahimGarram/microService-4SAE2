package com.example.freelanceplatformspringapp.Complaints.Services;
import com.example.freelanceplatformspringapp.Complaints.Entity.Complaints;
import com.example.freelanceplatformspringapp.Complaints.Entity.ClaimStatus;
import com.example.freelanceplatformspringapp.Complaints.Repository.ComplaintsRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ComplaintsService implements IComplaintsInterface {
    private final ComplaintsRepository cr;
    private final AutoAssignmentService autoAssignmentService;
    private final EmailService emailService;
    private final RestTemplate restTemplate;
    private final PenaltyRulesEngine penaltyRulesEngine;

    public ComplaintsService(ComplaintsRepository cr, AutoAssignmentService autoAssignmentService, EmailService emailService, RestTemplate restTemplate, PenaltyRulesEngine penaltyRulesEngine) {
        this.cr = cr;
        this.autoAssignmentService = autoAssignmentService;
        this.emailService = emailService;
        this.restTemplate = restTemplate;
        this.penaltyRulesEngine = penaltyRulesEngine;
    }

    @Override
    public Complaints addClaim(Complaints complaints) {
        if (complaints.getCreatedAt() == null) {
            complaints.setCreatedAt(LocalDateTime.now());
        }
        if (complaints.getClaimStatus() == null) {
            complaints.setClaimStatus(ClaimStatus.Pending);
        }
        if (complaints.getClaimPriority() == null) {
            complaints.setClaimPriority(com.example.freelanceplatformspringapp.Complaints.Entity.ClaimPriority.Medium);
        }
        
        // Set isVisible to true by default
        complaints.setVisible(true);

        // Auto-assignment and categorization
        autoAssignmentService.processNewComplaint(complaints);

        if (complaints.getClaimAttachment() != null) {
            if (complaints.getClaimAttachment().getUploadedAt() == null) {
                complaints.getClaimAttachment().setUploadedAt(LocalDateTime.now());
            }
            if (complaints.getClaimAttachment().getUploadedById() == null) {
                complaints.getClaimAttachment().setUploadedById(complaints.getUserId());
            }
        }

        Complaints saved = cr.save(complaints);

        // Optional: keep legacy claimId field in attachment in sync
        if (saved.getClaimAttachment() != null && saved.getClaimAttachment().getClaimId() == null) {
            saved.getClaimAttachment().setClaimId(saved.getIdReclamation());
            saved = cr.save(saved);
        }

        // Send email notifications
        try {
            String userEmail = getUserEmail(saved.getUserId());
            if (userEmail != null) {
                // Send complaint creation notification to user
                emailService.sendComplaintCreatedNotification(userEmail, saved);
            }
            
            // Send assignment notification to admin
            if (saved.getAssignedToAdminId() != null) {
                String adminEmail = getUserEmail(saved.getAssignedToAdminId());
                if (adminEmail != null) {
                    emailService.sendAssignmentNotification(adminEmail, saved);
                }
            }
        } catch (Exception e) {
            // Log but don't fail the complaint creation
            System.err.println("Failed to send email notifications: " + e.getMessage());
        }

        return saved;
    }

    @Override
    public Complaints addClaimWithEmail(Complaints complaints, String userEmail) {
        // Save the complaint first
        Complaints saved = addClaim(complaints);
        
        // Evaluate penalty rules for the user
        try {
            penaltyRulesEngine.evaluateRulesForUser(saved.getUserId());
        } catch (Exception e) {
            System.err.println("Failed to evaluate penalty rules: " + e.getMessage());
        }
        
        // Send email notification if userEmail is provided
        if (userEmail != null && !userEmail.isEmpty()) {
            try {
                emailService.sendComplaintCreatedNotification(userEmail, saved);
                System.out.println("Email sent successfully to: " + userEmail);
            } catch (Exception e) {
                // Log but don't fail the complaint creation
                System.err.println("Failed to send email notification to " + userEmail + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No email provided, skipping email notification");
        }
        
        return saved;
    }

    @Override
    public Optional<Complaints> retrieveClaim(Long id) {
        return cr.findById(id);
    }

    @Override
    public List<Complaints> retrieveAllComplaints() {
        return cr.findAll();
    }
    
    @Override
    public List<Complaints> retrieveAllVisibleComplaints() {
        return cr.findByIsVisibleTrue();
    }

    @Override
    public List<Complaints> retrieveComplaintsByUser(Long userId) {
        // Only return visible complaints
        return cr.findByUserIdAndIsVisibleTrue(userId);
    }

    @Override
    public Optional<Complaints> retrieveClaimForUser(Long id, Long userId) {
        // Only return if visible
        return cr.findByIdReclamationAndUserIdAndIsVisibleTrue(id, userId);
    }

    @Override
    public Complaints updateComplaint(Complaints complaints) {
        // Get the existing complaint to preserve dates and track changes
        Optional<Complaints> existingOpt = cr.findById(complaints.getIdReclamation());
        
        ClaimStatus previousStatus = null;
        String previousResolutionNote = null;
        
        if (existingOpt.isPresent()) {
            Complaints existing = existingOpt.get();
            previousStatus = existing.getClaimStatus();
            previousResolutionNote = existing.getResolutionNote();
            
            // Preserve createdAt - never change it
            complaints.setCreatedAt(existing.getCreatedAt());
            
            // Preserve isVisible - only soft-delete endpoint (deleteComplaint) may set it to false
            complaints.setVisible(existing.isVisible());
            
            // Always set updatedAt to now
            complaints.setUpdatedAt(new Date());
            
            // Set resolvedAt when status changes to Resolved
            if (complaints.getClaimStatus() == ClaimStatus.Resolved && 
                existing.getClaimStatus() != ClaimStatus.Resolved) {
                complaints.setResolvedAt(new Date());
            } else {
                // Preserve existing resolvedAt if already set
                complaints.setResolvedAt(existing.getResolvedAt());
            }
            
            // Preserve attachment if not provided in update
            if (complaints.getClaimAttachment() == null && existing.getClaimAttachment() != null) {
                complaints.setClaimAttachment(existing.getClaimAttachment());
            }
        } else {
            // If complaint doesn't exist, set updatedAt
            complaints.setUpdatedAt(new Date());
        }
        
        Complaints updated = cr.save(complaints);
        
        // Send email notifications
        try {
            String userEmail = getUserEmail(updated.getUserId());
            if (userEmail != null) {
                // Notify on status change
                if (previousStatus != null && !previousStatus.equals(updated.getClaimStatus())) {
                    emailService.sendStatusChangeNotification(userEmail, updated, previousStatus.toString());
                }
                
                // Notify on resolution note added/updated
                if (updated.getResolutionNote() != null && 
                    !updated.getResolutionNote().equals(previousResolutionNote)) {
                    emailService.sendResolutionNoteNotification(userEmail, updated);
                }
            }
        } catch (Exception e) {
            // Log but don't fail the update
            System.err.println("Failed to send email notification: " + e.getMessage());
        }
        
        return updated;
    }
    
    private String getUserEmail(Long userId) {
        try {
            String userServiceUrl = "http://localhost:8081/api/users/" + userId;
            var response = restTemplate.getForObject(userServiceUrl, java.util.Map.class);
            if (response != null && response.containsKey("email")) {
                return (String) response.get("email");
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch email for user ID: " + userId);
        }
        return null;
    }

    @Override
    public void deleteComplaint(Long id) {
        // Soft delete: set isVisible to false instead of actually deleting
        Optional<Complaints> complaintOpt = cr.findById(id);
        if (complaintOpt.isPresent()) {
            Complaints complaint = complaintOpt.get();
            complaint.setVisible(false);
            complaint.setUpdatedAt(new Date());
            cr.save(complaint);
        }
    }
}