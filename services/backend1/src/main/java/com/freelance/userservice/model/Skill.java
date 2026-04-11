package com.freelance.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * User Skill/Tag Entity
 * Represents a skill that a user (especially freelancers) possesses
 */
@Entity
@Table(name = "user_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user"})
public class Skill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * User who has this skill
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Skill name (e.g., "Java", "React", "Graphic Design")
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * Skill level (1-5 or "Beginner", "Intermediate", "Advanced", "Expert")
     */
    @Column(length = 50)
    private String level;
    
    /**
     * Years of experience with this skill
     */
    private Integer yearsOfExperience;
}
