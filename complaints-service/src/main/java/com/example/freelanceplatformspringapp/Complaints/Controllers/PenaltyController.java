package com.example.freelanceplatformspringapp.Complaints.Controllers;

import com.example.freelanceplatformspringapp.Complaints.Entity.Penalty;
import com.example.freelanceplatformspringapp.Complaints.Entity.PenaltySeverity;
import com.example.freelanceplatformspringapp.Complaints.Entity.PenaltyType;
import com.example.freelanceplatformspringapp.Complaints.Services.PenaltyRulesEngine;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/report/penalties")
@CrossOrigin(
    origins = "http://localhost:4200", 
    allowCredentials = "true",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
    allowedHeaders = "*"
)
public class PenaltyController {

    private final PenaltyRulesEngine penaltyRulesEngine;

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
    @lombok.Data
    public static class ManualPenaltyRequest {
        private Long userId;
        private Long complaintId;
        private PenaltyType type;
        private PenaltySeverity severity;
        private String reason;
        private String description;
        private Long adminId;
        private Integer daysToExpire;
    }
}
