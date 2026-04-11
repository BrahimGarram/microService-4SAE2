package com.example.freelanceplatformspringapp.Complaints.Controllers;

import com.example.freelanceplatformspringapp.Complaints.Services.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/report/test")
@CrossOrigin(
    origins = "http://localhost:4200", 
    allowCredentials = "true",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
    allowedHeaders = "*"
)
public class EmailTestController {

    private final EmailService emailService;

    /**
     * Test endpoint to send a test email
     * Usage: GET http://localhost:8089/freelancity/report/test/send-email?email=test@example.com
     */
    @GetMapping("/send-email")
    public String sendTestEmail(@RequestParam("email") String email) {
        try {
            // Create a dummy complaint for testing
            com.example.freelanceplatformspringapp.Complaints.Entity.Complaints testComplaint = 
                new com.example.freelanceplatformspringapp.Complaints.Entity.Complaints();
            testComplaint.setIdReclamation(999L);
            testComplaint.setTitle("Test Complaint");
            testComplaint.setDescription("This is a test complaint for email verification");
            testComplaint.setClaimPriority(com.example.freelanceplatformspringapp.Complaints.Entity.ClaimPriority.High);
            testComplaint.setClaimStatus(com.example.freelanceplatformspringapp.Complaints.Entity.ClaimStatus.Pending);
            testComplaint.setCategory(com.example.freelanceplatformspringapp.Complaints.Entity.ClaimCategory.TECHNICAL);
            
            emailService.sendComplaintCreatedNotification(email, testComplaint);
            
            return "Test email sent successfully to: " + email + ". Check your inbox (and spam folder).";
        } catch (Exception e) {
            return "Failed to send test email: " + e.getMessage() + ". Check application logs for details.";
        }
    }

    /**
     * Test endpoint to check email configuration
     * Usage: GET http://localhost:8089/freelancity/report/test/email-config
     */
    @GetMapping("/email-config")
    public String checkEmailConfig() {
        return "Email service is configured and ready. Use /send-email?email=your@email.com to test.";
    }
}
