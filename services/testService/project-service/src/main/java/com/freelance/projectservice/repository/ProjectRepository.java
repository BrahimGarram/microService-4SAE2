package com.freelance.projectservice.repository;

import com.freelance.projectservice.model.Project;
import com.freelance.projectservice.model.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
}
