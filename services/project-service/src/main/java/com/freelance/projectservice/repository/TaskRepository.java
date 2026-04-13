package com.freelance.projectservice.repository;

import com.freelance.projectservice.model.Task;
import com.freelance.projectservice.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * Find all tasks for a specific project
     */
    List<Task> findByProjectIdOrderByOrderIndexAsc(Long projectId);
    
    /**
     * Find tasks by project and status
     */
    List<Task> findByProjectIdAndStatusOrderByOrderIndexAsc(Long projectId, TaskStatus status);
    
    /**
     * Find tasks assigned to a specific user
     */
    List<Task> findByAssignedToOrderByCreatedAtDesc(Long userId);
    
    /**
     * Count tasks by project and status
     */
    long countByProjectIdAndStatus(Long projectId, TaskStatus status);
    
    /**
     * Count all tasks for a project
     */
    long countByProjectId(Long projectId);
    
    /**
     * Delete all tasks for a project (when project is deleted)
     */
    void deleteByProjectId(Long projectId);
}
