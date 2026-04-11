package com.example.freelanceplatformspringapp.Complaints.Services;

import com.example.freelanceplatformspringapp.Complaints.Entity.Complaints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@freelancify.com}")
    private String fromEmail;
    
    @Value("${app.base-url:http://localhost:4200}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        log.info("EmailService initialized successfully");
        log.info("Email will be sent from: {}", fromEmail);
        log.info("Base URL: {}", baseUrl);
    }

    /**
     * Get HTML email template header
     */
    private String getEmailHeader() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Freelancify Notification</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%); padding: 40px 20px; }
                    .email-container { max-width: 600px; margin: 0 auto; background: white; border-radius: 24px; overflow: hidden; box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15); }
                    .email-header { background: linear-gradient(135deg, #FF6B35 0%, #FF8C42 100%); padding: 40px 30px; text-align: center; position: relative; }
                    .email-header::before { content: ''; position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 320"><path fill="rgba(255,255,255,0.1)" d="M0,96L48,112C96,128,192,160,288,160C384,160,480,128,576,112C672,96,768,96,864,112C960,128,1056,160,1152,160C1248,160,1344,128,1392,112L1440,96L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path></svg>') no-repeat bottom; background-size: cover; opacity: 0.3; }
                    .logo-container { position: relative; z-index: 1; margin-bottom: 20px; }
                    .logo-wrapper { width: 100px; height: 100px; background: white; border-radius: 50%; display: inline-flex; align-items: center; justify-content: center; box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2); padding: 15px; }
                    .logo-img { width: 100%; height: 100%; object-fit: contain; }
                    .brand-name { color: white; font-size: 32px; font-weight: 800; margin-top: 15px; letter-spacing: -0.5px; position: relative; z-index: 1; text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2); }
                    .email-body { padding: 40px 30px; }
                    .greeting { font-size: 24px; font-weight: 700; color: #1a202c; margin-bottom: 20px; }
                    .message { font-size: 16px; color: #4a5568; line-height: 1.6; margin-bottom: 30px; }
                    .details-card { background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); border-radius: 16px; padding: 25px; margin: 30px 0; border-left: 5px solid #FF6B35; }
                    .details-title { font-size: 18px; font-weight: 700; color: #1a202c; margin-bottom: 20px; display: flex; align-items: center; gap: 10px; }
                    .details-title::before { content: '📋'; font-size: 24px; }
                    .detail-row { display: flex; padding: 12px 0; border-bottom: 1px solid #e2e8f0; }
                    .detail-row:last-child { border-bottom: none; }
                    .detail-label { font-weight: 700; color: #64748b; min-width: 120px; font-size: 14px; text-transform: uppercase; letter-spacing: 0.5px; }
                    .detail-value { color: #1e293b; font-weight: 600; flex: 1; font-size: 15px; }
                    .status-badge { display: inline-block; padding: 6px 16px; border-radius: 20px; font-size: 13px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; }
                    .status-pending { background: rgba(237, 137, 54, 0.15); color: #ed8936; }
                    .status-under-review { background: rgba(66, 153, 225, 0.15); color: #4299e1; }
                    .status-resolved { background: rgba(72, 187, 120, 0.15); color: #38a169; }
                    .status-rejected { background: rgba(229, 62, 62, 0.15); color: #e53e3e; }
                    .status-closed { background: rgba(160, 174, 192, 0.15); color: #4a5568; }
                    .priority-urgent { background: linear-gradient(135deg, rgba(229, 62, 62, 0.2), rgba(245, 101, 101, 0.2)); color: #c53030; }
                    .priority-high { background: linear-gradient(135deg, rgba(255, 107, 53, 0.2), rgba(237, 137, 54, 0.2)); color: #dd6b20; }
                    .priority-medium { background: rgba(66, 153, 225, 0.15); color: #3182ce; }
                    .priority-low { background: rgba(160, 174, 192, 0.15); color: #4a5568; }
                    .cta-button { display: inline-block; padding: 16px 40px; background: linear-gradient(135deg, #FF6B35, #FF8C42); color: white; text-decoration: none; border-radius: 12px; font-weight: 700; font-size: 16px; margin: 20px 0; box-shadow: 0 8px 20px rgba(255, 107, 53, 0.3); transition: all 0.3s ease; }
                    .cta-button:hover { box-shadow: 0 12px 28px rgba(255, 107, 53, 0.4); transform: translateY(-2px); }
                    .info-box { background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%); border-left: 4px solid #0ea5e9; padding: 20px; border-radius: 12px; margin: 25px 0; }
                    .info-box-icon { font-size: 24px; margin-bottom: 10px; }
                    .info-box-text { color: #0c4a6e; font-size: 15px; line-height: 1.6; }
                    .email-footer { background: #1a202c; padding: 30px; text-align: center; color: #94a3b8; }
                    .footer-text { font-size: 14px; line-height: 1.6; margin-bottom: 15px; }
                    .social-links { margin: 20px 0; }
                    .social-link { display: inline-block; width: 40px; height: 40px; background: rgba(255, 255, 255, 0.1); border-radius: 50%; margin: 0 8px; line-height: 40px; color: white; text-decoration: none; transition: all 0.3s ease; }
                    .social-link:hover { background: #FF6B35; transform: translateY(-3px); }
                    .divider { height: 1px; background: linear-gradient(90deg, transparent, #e2e8f0, transparent); margin: 30px 0; }
                    @media only screen and (max-width: 600px) {
                        .email-body { padding: 30px 20px; }
                        .greeting { font-size: 20px; }
                        .message { font-size: 15px; }
                        .detail-row { flex-direction: column; gap: 5px; }
                        .detail-label { min-width: auto; }
                        .logo-wrapper { width: 80px; height: 80px; }
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <div class="email-header">
                        <div class="logo-container">
                            <div class="logo-wrapper">
                                <img src="https://i.ibb.co/W4ymxgZW/Asset-1.png" alt="Freelancify Logo" class="logo-img" />
                            </div>
                        </div>
                        <div class="brand-name">Freelancify</div>
                    </div>
                    <div class="email-body">
            """;
    }

    /**
     * Get HTML email template footer
     */
    private String getEmailFooter() {
        return """
                    </div>
                    <div class="email-footer">
                        <div class="footer-text">
                            <strong>Freelancify Support Team</strong><br>
                            Your trusted freelance marketplace
                        </div>
                        <div class="divider" style="background: rgba(255,255,255,0.1);"></div>
                        <div class="footer-text" style="font-size: 13px; color: #64748b;">
                            This is an automated message. Please do not reply to this email.<br>
                            © 2024 Freelancify. All rights reserved.
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """;
    }

    /**
     * Send HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }

    /**
     * Get status badge HTML
     */
    private String getStatusBadge(String status) {
        String badgeClass = switch (status) {
            case "Pending" -> "status-pending";
            case "Under_Review" -> "status-under-review";
            case "Resolved" -> "status-resolved";
            case "Rejected" -> "status-rejected";
            case "Closed" -> "status-closed";
            default -> "status-pending";
        };
        String displayStatus = status.equals("Under_Review") ? "Under Review" : status;
        return "<span class=\"status-badge " + badgeClass + "\">" + displayStatus + "</span>";
    }

    /**
     * Get priority badge HTML
     */
    private String getPriorityBadge(String priority) {
        String badgeClass = switch (priority) {
            case "Urgent" -> "priority-urgent";
            case "High" -> "priority-high";
            case "Medium" -> "priority-medium";
            case "Low" -> "priority-low";
            default -> "priority-medium";
        };
        String emoji = switch (priority) {
            case "Urgent" -> "🔴";
            case "High" -> "🟠";
            case "Medium" -> "🟡";
            case "Low" -> "🟢";
            default -> "🟡";
        };
        return "<span class=\"status-badge " + badgeClass + "\">" + emoji + " " + priority + "</span>";
    }


    /**
     * Send email notification when new complaint is created
     */
    public void sendComplaintCreatedNotification(String userEmail, Complaints complaint) {
        sendComplaintCreatedNotification(userEmail, complaint, null);
    }
    
    /**
     * Send email notification when new complaint is created (with username)
     */
    public void sendComplaintCreatedNotification(String userEmail, Complaints complaint, String username) {
        try {
            String greeting = (username != null && !username.isEmpty()) ? username : "User";
            String htmlContent = getEmailHeader() + String.format("""
                <div class="greeting">Dear %s,</div>
                <div class="message">
                    Thank you for submitting your complaint. We have received it and our team will review it shortly.
                </div>
                
                <div class="details-card">
                    <div class="details-title">Complaint Details</div>
                    <div class="detail-row">
                        <div class="detail-label">Reference</div>
                        <div class="detail-value">#%d</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Title</div>
                        <div class="detail-value">%s</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Description</div>
                        <div class="detail-value">%s</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Priority</div>
                        <div class="detail-value">%s</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Status</div>
                        <div class="detail-value">%s</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Category</div>
                        <div class="detail-value">%s</div>
                    </div>
                </div>
                
                <div class="info-box">
                    <div class="info-box-icon">📧</div>
                    <div class="info-box-text">
                        You will receive email notifications when there are updates to your complaint.
                    </div>
                </div>
                
                <div style="text-align: center;">
                    <a href="%s/complaints" class="cta-button">View Your Complaint</a>
                </div>
                
                <div class="message" style="margin-top: 30px; font-size: 15px;">
                    Best regards,<br>
                    <strong>Freelancify Support Team</strong>
                </div>
                """,
                greeting,
                complaint.getIdReclamation(),
                complaint.getTitle(),
                complaint.getDescription(),
                getPriorityBadge(complaint.getClaimPriority().toString()),
                getStatusBadge(complaint.getClaimStatus().toString()),
                complaint.getCategory() != null ? complaint.getCategory() : "BILLING",
                baseUrl
            ) + getEmailFooter();
            
            sendHtmlEmail(userEmail, "Complaint Received - #" + complaint.getIdReclamation(), htmlContent);
            log.info("Complaint creation notification sent to: {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send complaint creation notification to: {}", userEmail, e);
        }
    }

    /**
     * Send email notification when complaint status changes
     */
    public void sendStatusChangeNotification(String userEmail, Complaints complaint, String previousStatus) {
        try {
            String htmlContent = getEmailHeader() + String.format("""
                <div class="greeting">Dear User,</div>
                <div class="message">
                    Your complaint status has been updated. Here are the details:   
                </div>
                
                <div class="details-card">
                    <div class="details-title">Status Update</div>
                    <div class="detail-row">
                        <div class="detail-label">Complaint ID</div>
                        <div class="detail-value">#%d</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Title</div>
                        <div class="detail-value">%s</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Previous Status</div>
                        <div class="detail-value">%s</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">New Status</div>
                        <div class="detail-value">%s</div>
                    </div>
                </div>
                
                <div style="text-align: center;">
                    <a href="%s/complaints" class="cta-button">View Complaint Details</a>
                </div>
                
                <div class="message" style="margin-top: 30px; font-size: 15px;">
                    Best regards,<br>
                    <strong>Freelancify Support Team</strong>
                </div>
                """,
                complaint.getIdReclamation(),
                complaint.getTitle(),
                getStatusBadge(previousStatus),
                getStatusBadge(complaint.getClaimStatus().toString()),
                baseUrl
            ) + getEmailFooter();
            
            sendHtmlEmail(userEmail, "Complaint Status Updated - #" + complaint.getIdReclamation(), htmlContent);
            log.info("Status change notification sent to: {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send status change notification to: {}", userEmail, e);
        }
    }

    /**
     * Send email notification when admin adds resolution note
     */
    public void sendResolutionNoteNotification(String userEmail, Complaints complaint) {
        try {
            String htmlContent = getEmailHeader() + String.format("""
                <div class="greeting">Dear User,</div>
                <div class="message">
                    An admin has responded to your complaint. Please review the response below:
                </div>
                
                <div class="details-card">
                    <div class="details-title">Complaint Information</div>
                    <div class="detail-row">
                        <div class="detail-label">Complaint ID</div>
                        <div class="detail-value">#%d</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Title</div>
                        <div class="detail-value">%s</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Status</div>
                        <div class="detail-value">%s</div>
                    </div>
                </div>
                
                <div class="details-card" style="background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%); border-left-color: #f59e0b;">
                    <div class="details-title" style="color: #92400e;">💬 Admin Response</div>
                    <div style="color: #78350f; font-size: 15px; line-height: 1.6; padding: 10px 0;">
                        %s
                    </div>
                </div>
                
                <div style="text-align: center;">
                    <a href="%s/complaints" class="cta-button">View Full Details</a>
                </div>
                
                <div class="message" style="margin-top: 30px; font-size: 15px;">
                    Best regards,<br>
                    <strong>Freelancify Support Team</strong>
                </div>
                """,
                complaint.getIdReclamation(),
                complaint.getTitle(),
                getStatusBadge(complaint.getClaimStatus().toString()),
                complaint.getResolutionNote(),
                baseUrl
            ) + getEmailFooter();
            
            sendHtmlEmail(userEmail, "Admin Response to Your Complaint - #" + complaint.getIdReclamation(), htmlContent);
            log.info("Resolution note notification sent to: {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send resolution note notification to: {}", userEmail, e);
        }
    }

    /**
     * Send reminder email for pending complaints
     */
    public void sendPendingComplaintReminder(String userEmail, Complaints complaint) {
        try {
            String htmlContent = getEmailHeader() + String.format("""
                <div class="greeting">Dear User,</div>
                <div class="message">
                    This is a friendly reminder that your complaint is still pending review.
                </div>
                
                <div class="details-card">
                    <div class="details-title">Complaint Details</div>
                    <div class="detail-row">
                        <div class="detail-label">Complaint ID</div>
                        <div class="detail-value">#%d</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Title</div>
                        <div class="detail-value">%s</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Status</div>
                        <div class="detail-value">%s</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Created</div>
                        <div class="detail-value">%s</div>
                    </div>
                </div>
                
                <div class="info-box">
                    <div class="info-box-icon">⏰</div>
                    <div class="info-box-text">
                        Our team is working on your complaint. We'll update you as soon as there's progress.
                    </div>
                </div>
                
                <div style="text-align: center;">
                    <a href="%s/complaints" class="cta-button">Check Status</a>
                </div>
                
                <div class="message" style="margin-top: 30px; font-size: 15px;">
                    Best regards,<br>
                    <strong>Freelancify Support Team</strong>
                </div>
                """,
                complaint.getIdReclamation(),
                complaint.getTitle(),
                getStatusBadge(complaint.getClaimStatus().toString()),
                complaint.getCreatedAt(),
                baseUrl
            ) + getEmailFooter();
            
            sendHtmlEmail(userEmail, "Reminder: Your Complaint is Pending - #" + complaint.getIdReclamation(), htmlContent);
            log.info("Pending complaint reminder sent to: {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send pending complaint reminder to: {}", userEmail, e);
        }
    }

    /**
     * Send auto-close notification
     */
    public void sendAutoCloseNotification(String userEmail, Complaints complaint) {
        try {
            String htmlContent = getEmailHeader() + String.format("""
                <div class="greeting">Dear User,</div>
                <div class="message">
                    Your complaint has been automatically closed due to inactivity.
                </div>
                
                <div class="details-card">
                    <div class="details-title">Closed Complaint</div>
                    <div class="detail-row">
                        <div class="detail-label">Complaint ID</div>
                        <div class="detail-value">#%d</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Title</div>
                        <div class="detail-value">%s</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Status</div>
                        <div class="detail-value">%s</div>
                    </div>
                </div>
                
                <div class="info-box" style="background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%); border-left-color: #ef4444;">
                    <div class="info-box-icon">ℹ️</div>
                    <div class="info-box-text" style="color: #7f1d1d;">
                        If you still need assistance, please create a new complaint and we'll be happy to help.
                    </div>
                </div>
                
                <div style="text-align: center;">
                    <a href="%s/complaints" class="cta-button">View All Complaints</a>
                </div>
                
                <div class="message" style="margin-top: 30px; font-size: 15px;">
                    Best regards,<br>
                    <strong>Freelancify Support Team</strong>
                </div>
                """,
                complaint.getIdReclamation(),
                complaint.getTitle(),
                getStatusBadge("Closed"),
                baseUrl
            ) + getEmailFooter();
            
            sendHtmlEmail(userEmail, "Complaint Auto-Closed - #" + complaint.getIdReclamation(), htmlContent);
            log.info("Auto-close notification sent to: {}", userEmail);
        } catch (Exception e) {
            log.error("Failed to send auto-close notification to: {}", userEmail, e);
        }
    }

    /**
     * Send auto-assignment notification to admin
     */
    public void sendAssignmentNotification(String adminEmail, Complaints complaint) {
        try {
            String htmlContent = getEmailHeader() + String.format("""
                <div class="greeting">Dear Admin,</div>
                <div class="message">
                    A new complaint has been assigned to you for review.
                </div>
                
                <div class="details-card">
                    <div class="details-title">Assigned Complaint</div>
                    <div class="detail-row">
                        <div class="detail-label">Complaint ID</div>
                        <div class="detail-value">#%d</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Title</div>
                        <div class="detail-value">%s</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Category</div>
                        <div class="detail-value">%s</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Priority</div>
                        <div class="detail-value">%s</div>
                    </div>
                    <div class="detail-row">
                        <div class="detail-label">Description</div>
                        <div class="detail-value">%s</div>
                    </div>
                </div>
                
                <div style="text-align: center;">
                    <a href="%s/admin/complaints" class="cta-button">Review Complaint</a>
                </div>
                
                <div class="message" style="margin-top: 30px; font-size: 15px;">
                    Best regards,<br>
                    <strong>Freelancify System</strong>
                </div>
                """,
                complaint.getIdReclamation(),
                complaint.getTitle(),
                complaint.getCategory(),
                getPriorityBadge(complaint.getClaimPriority().toString()),
                complaint.getDescription(),
                baseUrl
            ) + getEmailFooter();
            
            sendHtmlEmail(adminEmail, "New Complaint Assigned - #" + complaint.getIdReclamation(), htmlContent);
            log.info("Assignment notification sent to admin: {}", adminEmail);
        } catch (Exception e) {
            log.error("Failed to send assignment notification to admin: {}", adminEmail, e);
        }
    }
}

