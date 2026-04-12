package com.freelance.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * User Language Entity
 * Represents languages spoken by a user
 */
@Entity
@Table(name = "user_languages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user"})
public class Language {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * User who speaks this language
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Language name (e.g., "English", "French", "Arabic")
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * Language code (e.g., "en", "fr", "ar")
     */
    @Column(length = 10)
    private String code;
    
    /**
     * Proficiency level (e.g., "Native", "Fluent", "Intermediate", "Basic")
     */
    @Column(length = 50)
    private String proficiency;
}
