package com.freelance.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Social Link Entity
 * Represents social media links for a user
 */
@Entity
@Table(name = "social_links")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user"})
public class SocialLink {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * User who owns this social link
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Platform name (e.g., "LinkedIn", "GitHub", "Twitter", "Facebook")
     */
    @Column(nullable = false, length = 50)
    private String platform;
    
    /**
     * URL to the social profile
     */
    @Column(nullable = false, length = 500)
    private String url;
    
    /**
     * Username on the platform (optional)
     */
    @Column(length = 100)
    private String username;
}
