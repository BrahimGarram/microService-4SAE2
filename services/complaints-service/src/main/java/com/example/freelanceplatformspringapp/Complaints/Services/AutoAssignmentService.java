package com.example.freelanceplatformspringapp.Complaints.Services;

import com.example.freelanceplatformspringapp.Complaints.Entity.ClaimCategory;
import com.example.freelanceplatformspringapp.Complaints.Entity.Complaints;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AutoAssignmentService {

    // Map categories to admin IDs (you can make this configurable via database)
    private final Map<ClaimCategory, Long> categoryToAdminMap = new HashMap<>();
    
    // Keywords for auto-categorization
    private final Map<String, ClaimCategory> keywordToCategoryMap = new HashMap<>();

    public AutoAssignmentService() {
        // Initialize category to admin mapping
        // TODO: Load from database or configuration
        categoryToAdminMap.put(ClaimCategory.TECHNICAL, 1L);
        categoryToAdminMap.put(ClaimCategory.BILLING, 2L);
        categoryToAdminMap.put(ClaimCategory.SERVICE, 1L);
        categoryToAdminMap.put(ClaimCategory.ACCOUNT, 2L);
        categoryToAdminMap.put(ClaimCategory.FEATURE, 1L);
        categoryToAdminMap.put(ClaimCategory.BUG, 1L);
        categoryToAdminMap.put(ClaimCategory.OTHER, 1L);

        // Initialize keyword to category mapping
        initializeKeywordMapping();
    }

    private void initializeKeywordMapping() {
        // Technical keywords
        keywordToCategoryMap.put("error", ClaimCategory.TECHNICAL);
        keywordToCategoryMap.put("bug", ClaimCategory.BUG);
        keywordToCategoryMap.put("crash", ClaimCategory.TECHNICAL);
        keywordToCategoryMap.put("not working", ClaimCategory.TECHNICAL);
        keywordToCategoryMap.put("broken", ClaimCategory.TECHNICAL);
        keywordToCategoryMap.put("issue", ClaimCategory.TECHNICAL);
        keywordToCategoryMap.put("problem", ClaimCategory.TECHNICAL);
        
        // Billing keywords
        keywordToCategoryMap.put("payment", ClaimCategory.BILLING);
        keywordToCategoryMap.put("invoice", ClaimCategory.BILLING);
        keywordToCategoryMap.put("charge", ClaimCategory.BILLING);
        keywordToCategoryMap.put("refund", ClaimCategory.BILLING);
        keywordToCategoryMap.put("billing", ClaimCategory.BILLING);
        keywordToCategoryMap.put("price", ClaimCategory.BILLING);
        
        // Account keywords
        keywordToCategoryMap.put("login", ClaimCategory.ACCOUNT);
        keywordToCategoryMap.put("password", ClaimCategory.ACCOUNT);
        keywordToCategoryMap.put("account", ClaimCategory.ACCOUNT);
        keywordToCategoryMap.put("profile", ClaimCategory.ACCOUNT);
        keywordToCategoryMap.put("access", ClaimCategory.ACCOUNT);
        
        // Service keywords
        keywordToCategoryMap.put("service", ClaimCategory.SERVICE);
        keywordToCategoryMap.put("quality", ClaimCategory.SERVICE);
        keywordToCategoryMap.put("support", ClaimCategory.SERVICE);
        keywordToCategoryMap.put("response", ClaimCategory.SERVICE);
        
        // Feature keywords
        keywordToCategoryMap.put("feature", ClaimCategory.FEATURE);
        keywordToCategoryMap.put("request", ClaimCategory.FEATURE);
        keywordToCategoryMap.put("suggestion", ClaimCategory.FEATURE);
        keywordToCategoryMap.put("improvement", ClaimCategory.FEATURE);
    }

    /**
     * Auto-detect category based on title and description keywords
     */
    public ClaimCategory detectCategory(String title, String description) {
        String combinedText = (title + " " + description).toLowerCase();
        
        // Check for keyword matches
        for (Map.Entry<String, ClaimCategory> entry : keywordToCategoryMap.entrySet()) {
            if (combinedText.contains(entry.getKey())) {
                log.info("Auto-detected category: {} based on keyword: {}", entry.getValue(), entry.getKey());
                return entry.getValue();
            }
        }
        
        // Default to OTHER if no match found
        log.info("No category match found, defaulting to OTHER");
        return ClaimCategory.OTHER;
    }

    /**
     * Auto-assign complaint to admin based on category
     */
    public Long assignToAdmin(ClaimCategory category) {
        Long adminId = categoryToAdminMap.getOrDefault(category, 1L);
        log.info("Auto-assigned complaint with category {} to admin ID: {}", category, adminId);
        return adminId;
    }

    /**
     * Process new complaint for auto-assignment
     */
    public void processNewComplaint(Complaints complaint) {
        // Auto-detect category if not set
        if (complaint.getCategory() == null) {
            ClaimCategory detectedCategory = detectCategory(
                complaint.getTitle(), 
                complaint.getDescription()
            );
            complaint.setCategory(detectedCategory);
        }
        
        // Auto-assign to admin
        if (complaint.getAssignedToAdminId() == null) {
            Long adminId = assignToAdmin(complaint.getCategory());
            complaint.setAssignedToAdminId(adminId);
        }
    }
}
