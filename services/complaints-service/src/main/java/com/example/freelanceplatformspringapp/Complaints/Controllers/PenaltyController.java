package com.example.freelanceplatformspringapp.Complaints.Controllers;

import com.example.freelanceplatformspringapp.Complaints.Entity.Penalty;
import com.example.freelanceplatformspringapp.Complaints.Entity.PenaltySeverity;
import com.example.freelanceplatformspringapp.Complaints.Entity.PenaltyType;
import com.example.freelanceplatformspringapp.Complaints.Services.PenaltyRulesEngine;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/report/penalties")
@CrossOrigin(
    origins = "http://localhost:4200", 
    allowCredentials = "true",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
    allowedHeaders = "*"
)
public class PenaltyController {

    private final PenaltyRulesEngine penaltyRulesEngine;

    public PenaltyController(PenaltyRulesEngine penaltyRulesEngine) {
        this.penaltyRulesEngine = penaltyRulesEngine;
    }

    /**
     * Evaluate penalty rules for a user
     * GET /report/penalties/evaluate?userId=1
     */
    @GetMapping("/evaluate")
    public List<Penalty> evaluateRulesForUser(@RequestParam("userId") Long userId) {
        return penaltyRulesEngine.evaluateRulesForUser(userId);
    }

    /**
     * Get all active penalties for a user
     * GET /report/penalties/user/1
     */
    @GetMapping("/user/{userId}")
    public List<Penalty> getActivePenalties(@PathVariable("userId") Long userId) {
        return penaltyRulesEngine.getActivePenaltiesForUser(userId);
    }

    /**
     * Check if user has active penalties
     * GET /report/penalties/check?userId=1
     */
    @GetMapping("/check")
    public boolean hasActivePenalties(@RequestParam("userId") Long userId) {
        return penaltyRulesEngine.hasActivePenalties(userId);
    }

    /**
     * Apply manual penalty (admin only)
     * POST /report/penalties/apply
     */
    @PostMapping("/apply")
    public Penalty applyManualPenalty(@RequestBody ManualPenaltyRequest request) {
        return penaltyRulesEngine.applyManualPenalty(
            request.getUserId(),
            request.getComplaintId(),
            request.getType(),
            request.getSeverity(),
            request.getReason(),
            request.getDescription(),
            request.getAdminId(),
            request.getDaysToExpire()
        );
    }

    /**
     * Deactivate a penalty (admin only)
     */
    @PostMapping("/deactivate/{penaltyId}")
    public Penalty deactivatePenalty(@PathVariable("penaltyId") Long penaltyId) {
        return penaltyRulesEngine.deactivatePenalty(penaltyId);
    }

    /**
     * Deactivate expired penalties (scheduled task or manual trigger)
     * POST /report/penalties/deactivate-expired
     */
    @PostMapping("/deactivate-expired")
    public String deactivateExpiredPenalties() {
        penaltyRulesEngine.deactivateExpiredPenalties();
        return "Expired penalties deactivated successfully";
    }

    // DTO for manual penalty request
    public static class ManualPenaltyRequest {
        private Long userId;
        private Long complaintId;
        private PenaltyType type;
        private PenaltySeverity severity;
        private String reason;
        private String description;
        private Long adminId;
        private Integer daysToExpire;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getComplaintId() { return complaintId; }
        public void setComplaintId(Long complaintId) { this.complaintId = complaintId; }
        public PenaltyType getType() { return type; }
        public void setType(PenaltyType type) { this.type = type; }
        public PenaltySeverity getSeverity() { return severity; }
        public void setSeverity(PenaltySeverity severity) { this.severity = severity; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Long getAdminId() { return adminId; }
        public void setAdminId(Long adminId) { this.adminId = adminId; }
        public Integer getDaysToExpire() { return daysToExpire; }
        public void setDaysToExpire(Integer daysToExpire) { this.daysToExpire = daysToExpire; }
    }
}
