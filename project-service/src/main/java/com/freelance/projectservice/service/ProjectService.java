package com.freelance.projectservice.service;

import com.freelance.projectservice.dto.CreateProjectRequest;
import com.freelance.projectservice.dto.ProjectDTO;
import com.freelance.projectservice.dto.UpdateProjectRequest;
import com.freelance.projectservice.exception.ResourceNotFoundException;
import com.freelance.projectservice.model.Project;
import com.freelance.projectservice.model.ProjectStatus;
import com.freelance.projectservice.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Project Service Layer
 * 
 * Contains business logic for project operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    
    /**
     * Create a new project
     */
    public ProjectDTO createProject(CreateProjectRequest request) {
        Project project = new Project();
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus() != null ? request.getStatus() : ProjectStatus.DRAFT);
        project.setOwnerId(request.getOwnerId());
        project.setBudget(request.getBudget());
        project.setDeadline(request.getDeadline());
        project.setCategory(request.getCategory());
        project.setImageUrl(request.getImageUrl());
        project.setTags(request.getTags());
        
        Project savedProject = projectRepository.save(project);
        log.info("Project created - ID: {}, Title: {}, Owner: {}, Category: {}", 
                savedProject.getId(), savedProject.getTitle(), savedProject.getOwnerId(), savedProject.getCategory());
        
        return convertToDTO(savedProject);
    }
    
    /**
     * Get project by ID
     */
    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long id) {
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return convertToDTO(project);
    }
    
    /**
     * Get all projects
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get projects by owner ID
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsByOwnerId(Long ownerId) {
        List<Project> projects = projectRepository.findByOwnerId(ownerId);
        return projects.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get projects by status
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsByStatus(ProjectStatus status) {
        List<Project> projects = projectRepository.findByStatus(status);
        return projects.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get projects by category
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsByCategory(String category) {
        List<Project> projects = projectRepository.findByCategory(category);
        return projects.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Update project
     */
    public ProjectDTO updateProject(Long id, UpdateProjectRequest request) {
        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            project.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }
        if (request.getBudget() != null) {
            project.setBudget(request.getBudget());
        }
        if (request.getDeadline() != null) {
            project.setDeadline(request.getDeadline());
        }
        if (request.getCategory() != null) {
            project.setCategory(request.getCategory());
        }
        if (request.getImageUrl() != null) {
            project.setImageUrl(request.getImageUrl());
        }
        if (request.getTags() != null) {
            project.setTags(request.getTags());
        }
        
        Project updatedProject = projectRepository.save(project);
        log.info("Project updated - ID: {}, Title: {}", updatedProject.getId(), updatedProject.getTitle());
        
        return convertToDTO(updatedProject);
    }
    
    /**
     * Delete project
     */
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }
        projectRepository.deleteById(id);
        log.info("Project deleted - ID: {}", id);
    }
    
    /**
     * Convert Project entity to ProjectDTO
     */
    private ProjectDTO convertToDTO(Project project) {
        return ProjectDTO.builder()
            .id(project.getId())
            .title(project.getTitle())
            .description(project.getDescription())
            .status(project.getStatus())
            .ownerId(project.getOwnerId())
            .budget(project.getBudget())
            .deadline(project.getDeadline())
            .category(project.getCategory())
            .imageUrl(project.getImageUrl())
            .tags(project.getTags())
            .createdAt(project.getCreatedAt())
            .updatedAt(project.getUpdatedAt())
            .build();
    }
}
