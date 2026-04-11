package com.freelance.userservice.dto;

import com.freelance.userservice.model.UserRole;
import com.freelance.userservice.model.UserAvailability;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * User Data Transfer Object
 * 
 * Used for transferring user data between layers.
 * Excludes sensitive information and provides a clean API response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    
    private Long id;
    private String keycloakId;
    private String username;
    private String email;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String country;
    
    // Profile fields
    private String profilePicture;
    private String bio;
    private String city;
    private String timezone;
    private BigDecimal hourlyRate;
    private UserAvailability availability;
    private Boolean verified;
    
    // Related entities
    private List<SkillDTO> skills;
    private List<PortfolioItemDTO> portfolioItems;
    private List<LanguageDTO> languages;
    private List<SocialLinkDTO> socialLinks;
    
    // Rating information
    private Double averageRating;
    private Long ratingCount;
    
    /**
     * Convert User entity to UserDTO
     */
    public static UserDTO fromEntity(com.freelance.userservice.model.User user) {
        return UserDTO.builder()
            .id(user.getId())
            .keycloakId(user.getKeycloakId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .country(user.getCountry())
            .profilePicture(user.getProfilePicture())
            .bio(user.getBio())
            .city(user.getCity())
            .timezone(user.getTimezone())
            .hourlyRate(user.getHourlyRate())
            .availability(user.getAvailability())
            .verified(user.getVerified())
            .build();
    }
}
