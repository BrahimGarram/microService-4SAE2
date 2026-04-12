package com.example.freelanceplatformspringapp.Complaints.Services;

import com.example.freelanceplatformspringapp.Complaints.Entity.ClaimStatus;
import com.example.freelanceplatformspringapp.Complaints.Entity.Complaints;
import com.example.freelanceplatformspringapp.Complaints.Repository.ComplaintsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class ComplaintScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ComplaintScheduledTasks.class);

    private final ComplaintsRepository complaintsRepository;
    private final EmailService emailService;
    private final RestTemplate restTemplate;
    private final PenaltyRulesEngine penaltyRulesEngine;

    public ComplaintScheduledTasks(ComplaintsRepository complaintsRepository, EmailService emailService, RestTemplate restTemplate, PenaltyRulesEngine penaltyRulesEngine) {
        this.complaintsRepository = complaintsRepository;
        this.emailService = emailService;
        this.restTemplate = restTemplate;
        this.penaltyRulesEngine = penaltyRulesEngine;
    }

    /**
     * Send reminder emails for pending complaints (runs every day at 9 AM)
     * Sends reminder if complaint is pending for more than 3 days and no reminder sent in last 24 hours
     */
    @Scheduled(cron = "0 0 9 * * *") // Every day at 9 AM
    @Transactional
    public void sendPendingComplaintReminders() {
        log.info("Starting scheduled task: Send pending complaint reminders");
        
        List<Complaints> allComplaints = complaintsRepository.findByIsVisibleTrue();
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        
        int remindersSent = 0;
        
        for (Complaints complaint : allComplaints) {
            // Check if complaint is pending or under review
            if ((complaint.getClaimStatus() == ClaimStatus.Pending || 
                 complaint.getClaimStatus() == ClaimStatus.Under_Review) &&
                complaint.getCreatedAt().isBefore(threeDaysAgo)) {
                
                // Check if reminder was already sent in last 24 hours
                boolean shouldSendReminder = true;
                if (complaint.getLastReminderSentAt() != null) {
                    LocalDateTime lastReminder = complaint.getLastReminderSentAt()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                    
                    if (lastReminder.isAfter(oneDayAgo)) {
                        shouldSendReminder = false;
                    }
                }
                
                if (shouldSendReminder) {
                    try {
                        // Fetch user email
                        String userEmail = getUserEmail(complaint.getUserId());
                        if (userEmail != null) {
                            emailService.sendPendingComplaintReminder(userEmail, complaint);
                            complaint.setLastReminderSentAt(new Date());
                            complaintsRepository.save(complaint);
                            remindersSent++;
                        }
                    } catch (Exception e) {
                        log.error("Failed to send reminder for complaint ID: {}", complaint.getIdReclamation(), e);
                    }
                }
            }
        }
        
        log.info("Completed scheduled task: Sent {} pending complaint reminders", remindersSent);
    }

    /**
     * Auto-close resolved complaints after 6 days of inactivity (runs every day at 2 AM)
     */
    @Scheduled(cron = "0 0 2 * * *") // Every day at 2 AM
    @Transactional
    public void autoCloseInactiveComplaints() {
        log.info("Starting scheduled task: Auto-close inactive complaints");
        
        List<Complaints> allComplaints = complaintsRepository.findByIsVisibleTrue();
        LocalDateTime sixDaysAgo = LocalDateTime.now().minusDays(6);
        
        int closedCount = 0;
        
        for (Complaints complaint : allComplaints) {
            // Auto-close resolved complaints that haven't been updated in 6 days
            if (complaint.getClaimStatus() == ClaimStatus.Resolved) {
                LocalDateTime lastUpdate = complaint.getUpdatedAt() != null 
                    ? complaint.getUpdatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    : complaint.getCreatedAt();
                
                if (lastUpdate.isBefore(sixDaysAgo)) {
                    try {
                        complaint.setClaimStatus(ClaimStatus.Closed);
                        complaint.setUpdatedAt(new Date());
                        complaintsRepository.save(complaint);
                        
                        // Send notification
                        String userEmail = getUserEmail(complaint.getUserId());
                        if (userEmail != null) {
                            emailService.sendAutoCloseNotification(userEmail, complaint);
                        }
                        
                        closedCount++;
                        log.info("Auto-closed complaint ID: {} due to 6 days of inactivity", complaint.getIdReclamation());
                    } catch (Exception e) {
                        log.error("Failed to auto-close complaint ID: {}", complaint.getIdReclamation(), e);
                    }
                }
            }
        }
        
        log.info("Completed scheduled task: Auto-closed {} inactive complaints", closedCount);
    }

    /**
     * Deactivate expired penalties (runs every day at 3 AM)
     */
    @Scheduled(cron = "0 0 3 * * *") // Every day at 3 AM
    @Transactional
    public void deactivateExpiredPenalties() {
        log.info("Starting scheduled task: Deactivate expired penalties");
        penaltyRulesEngine.deactivateExpiredPenalties();
        log.info("Completed scheduled task: Deactivate expired penalties");
    }

    /**
     * Fetch user email from user service
     */
    private String getUserEmail(Long userId) {
        try {
            String userServiceUrl = "http://localhost:8081/api/users/" + userId;
            // Assuming user service returns a user object with email field
            var response = restTemplate.getForObject(userServiceUrl, java.util.Map.class);
            if (response != null && response.containsKey("email")) {
                return (String) response.get("email");
            }
        } catch (Exception e) {
            log.error("Failed to fetch email for user ID: {}", userId, e);
        }
        return null;
    }
}
