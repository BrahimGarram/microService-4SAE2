package com.freelance.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Portfolio Item Entity
 * Represents a project or link in user's portfolio
 */
@Entity
@Table(name = "portfolio_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user"})
public class PortfolioItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * User who owns this portfolio item
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Title of the project/item
     */
    @Column(nullable = false, length = 200)
    private String title;
    
    /**
     * Description of the project
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * URL to the project (GitHub, website, etc.)
     */
    @Column(length = 500)
    private String url;
    
    /**
     * Image URL for the project thumbnail
     */
    @Column(length = 500)
    private String imageUrl;
    
    /**
     * Technologies used (comma-separated or JSON)
     */
    @Column(length = 500)
    private String technologies;
    
    /**
     * Date when the project was completed
     */
    private LocalDateTime completedDate;
    
    /**
     * Order/position for display
     */
    private Integer displayOrder;
}
