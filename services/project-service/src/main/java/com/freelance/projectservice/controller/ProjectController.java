package com.freelance.projectservice.controller;

import com.freelance.projectservice.dto.CreateProjectRequest;
import com.freelance.projectservice.dto.DraftFromTextRequest;
import com.freelance.projectservice.dto.ProjectDTO;
import com.freelance.projectservice.dto.ProjectDraftDTO;
import com.freelance.projectservice.dto.ProjectStatsDTO;
import com.freelance.projectservice.dto.UpdateProjectRequest;
import com.freelance.projectservice.model.ProjectStatus;
import com.freelance.projectservice.service.ProjectAiService;
import com.freelance.projectservice.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Project REST Controller
 * 
 * Provides REST endpoints for project management (CRUD operations).
 * No authentication - authentication is handled by user-service.
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectAiService projectAiService;

    @Value("${app.tokens-per-project:5}")
    private int tokensPerProject;
    @Value("${app.tokens-per-proposal:2}")
    private int tokensPerProposal;

    /**
     * GET /api/projects/config/token-costs
     * Returns token costs for project creation and proposal submission (for frontend display).
     */
    @GetMapping("/config/token-costs")
    public ResponseEntity<Map<String, Integer>> getTokenCosts() {
        return ResponseEntity.ok(Map.of("tokensPerProject", tokensPerProject, "tokensPerProposal", tokensPerProposal));
    }

    /**
     * POST /api/projects/ai/draft-from-text
     * Generate a project draft from natural language (Ollama).
     */
    @PostMapping("/ai/draft-from-text")
    public ResponseEntity<ProjectDraftDTO> draftFromText(@Valid @RequestBody DraftFromTextRequest request) {
        ProjectDraftDTO draft = projectAiService.draftFromText(request.getUserMessage());
        return ResponseEntity.ok(draft);
    }

    /**
     * POST /api/projects
     * Create a new project
     */
    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody CreateProjectRequest request) {
        ProjectDTO project = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }
    
    /**
     * GET /api/projects/stats
     * Get project statistics for backoffice (admin dashboard).
     */
    @GetMapping("/stats")
    public ResponseEntity<ProjectStatsDTO> getProjectStats() {
        ProjectStatsDTO stats = projectService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/projects/{id}
     * Get project by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        ProjectDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }
    
    /**
     * GET /api/projects
     * Get all projects
     */
    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        List<ProjectDTO> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }
    
    /**
     * GET /api/projects/owner/{ownerId}
     * Get projects by owner ID
     */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<ProjectDTO>> getProjectsByOwner(@PathVariable Long ownerId) {
        List<ProjectDTO> projects = projectService.getProjectsByOwnerId(ownerId);
        return ResponseEntity.ok(projects);
    }
    
    /**
     * GET /api/projects/status/{status}
     * Get projects by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProjectDTO>> getProjectsByStatus(@PathVariable ProjectStatus status) {
        List<ProjectDTO> projects = projectService.getProjectsByStatus(status);
        return ResponseEntity.ok(projects);
    }
    
    /**
     * GET /api/projects/category/{category}
     * Get projects by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProjectDTO>> getProjectsByCategory(@PathVariable String category) {
        List<ProjectDTO> projects = projectService.getProjectsByCategory(category);
        return ResponseEntity.ok(projects);
    }
    
    /**
     * PUT /api/projects/{id}
     * Update project
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request) {
        ProjectDTO project = projectService.updateProject(id, request);
        return ResponseEntity.ok(project);
    }
    
    /**
     * DELETE /api/projects/{id}
     * Delete project
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
