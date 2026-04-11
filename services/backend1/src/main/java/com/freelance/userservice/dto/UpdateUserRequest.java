package com.freelance.userservice.dto;

import com.freelance.userservice.model.UserAvailability;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for updating user profile
 * 
 * Only includes fields that can be updated by the user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    private String username;
    
    @Email(message = "Email must be valid")
    private String email;
    
    // Profile fields
    private String bio;
    private String city;
    private String timezone;
    private BigDecimal hourlyRate;
    private UserAvailability availability;
    
    // Related entities (will be managed separately via dedicated endpoints)
    private List<SkillDTO> skills;
    private List<PortfolioItemDTO> portfolioItems;
    private List<LanguageDTO> languages;
    private List<SocialLinkDTO> socialLinks;
}
