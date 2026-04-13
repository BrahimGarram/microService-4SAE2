package com.freelance.projectservice.repository;

import com.freelance.projectservice.model.Project;
import com.freelance.projectservice.model.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Project Repository
 * 
 * Provides data access methods for Project entity.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    /**
     * Find projects by owner ID
     */
    List<Project> findByOwnerId(Long ownerId);
    
    /**
     * Find projects by status
     */
    List<Project> findByStatus(ProjectStatus status);
    
    long countByStatus(ProjectStatus status);
    
    /**
     * Find projects by owner ID and status
     */
    List<Project> findByOwnerIdAndStatus(Long ownerId, ProjectStatus status);
    
    /**
     * Check if project exists by ID and owner ID
     */
    boolean existsByIdAndOwnerId(Long id, Long ownerId);
    
    /**
     * Find projects by category
     */
    List<Project> findByCategory(String category);
    
    @Query("SELECT COALESCE(SUM(p.budget), 0) FROM Project p WHERE p.budget IS NOT NULL")
    BigDecimal sumBudget();
    
    long countByBudgetIsNotNull();
    
    @Query("SELECT COALESCE(p.category, 'Uncategorized'), COUNT(p) FROM Project p GROUP BY COALESCE(p.category, 'Uncategorized')")
    List<Object[]> countByCategory();
}
