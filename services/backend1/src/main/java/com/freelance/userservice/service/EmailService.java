package com.freelance.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email Service
 * 
 * Handles sending emails for password reset and other notifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;
    
    @Value("${app.mail.from}")
    private String fromEmail;
    
    @Value("${app.mail.from-name}")
    private String fromName;
    
    /**
     * Send password reset email
     * @param toEmail Recipient email
     * @param resetToken Password reset token
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromName + " <" + fromEmail + ">");
            message.setTo(toEmail);
            message.setSubject("Reset Your Password - Freelancify");
            message.setText(buildPasswordResetEmailBody(resetLink));
            
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Error sending password reset email to: {}", toEmail, e);
            // Don't throw exception to avoid revealing if user exists
        }
    }
    
    /**
     * Build password reset email body
     */
    private String buildPasswordResetEmailBody(String resetLink) {
        return "Hello,\n\n" +
               "You have requested to reset your password for your Freelancify account.\n\n" +
               "Please click on the following link to reset your password:\n" +
               resetLink + "\n\n" +
               "This link will expire in 1 hour.\n\n" +
               "If you did not request this password reset, please ignore this email.\n\n" +
               "Best regards,\n" +
               "Freelancify Team";
    }
}
