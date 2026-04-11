package com.freelance.projectservice.controller;

import com.freelance.projectservice.dto.CreateTaskRequest;
import com.freelance.projectservice.dto.TaskDTO;
import com.freelance.projectservice.dto.UpdateTaskRequest;
import com.freelance.projectservice.model.TaskStatus;
import com.freelance.projectservice.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    /**
     * Create a new task
     */
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskDTO task = taskService.createTask(request);
        return new ResponseEntity<>(task, HttpStatus.CREATED);
    }
    
    /**
     * Get all tasks for a project
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskDTO>> getTasksByProject(@PathVariable Long projectId) {
        List<TaskDTO> tasks = taskService.getTasksByProject(projectId);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Get tasks by project and status
     */
    @GetMapping("/project/{projectId}/status/{status}")
    public ResponseEntity<List<TaskDTO>> getTasksByProjectAndStatus(
            @PathVariable Long projectId,
            @PathVariable TaskStatus status) {
        List<TaskDTO> tasks = taskService.getTasksByProjectAndStatus(projectId, status);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Get task by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        TaskDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }
    
    /**
     * Update a task
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskDTO task = taskService.updateTask(id, request);
        return ResponseEntity.ok(task);
    }
    
    /**
     * Delete a task
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get task statistics for a project
     */
    @GetMapping("/project/{projectId}/statistics")
    public ResponseEntity<Map<String, Object>> getTaskStatistics(@PathVariable Long projectId) {
        TaskService.TaskStatistics stats = taskService.getTaskStatistics(projectId);
        return ResponseEntity.ok(Map.of(
            "total", stats.getTotal(),
            "toDo", stats.getToDo(),
            "inProgress", stats.getInProgress(),
            "done", stats.getDone(),
            "completionRate", stats.getCompletionRate()
        ));
    }
}
