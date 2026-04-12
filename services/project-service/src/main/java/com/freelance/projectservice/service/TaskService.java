package com.freelance.projectservice.service;

import com.freelance.projectservice.dto.CreateTaskRequest;
import com.freelance.projectservice.dto.TaskDTO;
import com.freelance.projectservice.dto.UpdateTaskRequest;
import com.freelance.projectservice.exception.ResourceNotFoundException;
import com.freelance.projectservice.model.Task;
import com.freelance.projectservice.model.TaskStatus;
import com.freelance.projectservice.repository.ProjectRepository;
import com.freelance.projectservice.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    /**
     * Create a new task
     */
    public TaskDTO createTask(CreateTaskRequest request) {
        // Verify project exists
        if (!projectRepository.existsById(request.getProjectId())) {
            throw new ResourceNotFoundException("Project not found with id: " + request.getProjectId());
        }
        
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(blankToNull(request.getDescription()));
        task.setProjectId(request.getProjectId());
        task.setAssignedTo(request.getAssignedTo());
        task.setCreatedBy(request.getCreatedBy());
        task.setDueDate(parseDueDate(request.getDueDate()));
        task.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        task.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        task.setStatus(TaskStatus.TO_DO);
        
        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask);
    }
    
    /**
     * Get all tasks for a project
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByProject(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectIdOrderByOrderIndexAsc(projectId);
        return tasks.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get task by ID
     */
    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return convertToDTO(task);
    }
    
    /**
     * Update a task
     */
    public TaskDTO updateTask(Long id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getAssignedTo() != null) {
            task.setAssignedTo(request.getAssignedTo());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getOrderIndex() != null) {
            task.setOrderIndex(request.getOrderIndex());
        }
        
        Task updatedTask = taskRepository.save(task);
        return convertToDTO(updatedTask);
    }
    
    /**
     * Delete a task
     */
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }
    
    /**
     * Get tasks by status for a project
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByProjectAndStatus(Long projectId, TaskStatus status) {
        List<Task> tasks = taskRepository.findByProjectIdAndStatusOrderByOrderIndexAsc(projectId, status);
        return tasks.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get task statistics for a project
     */
    @Transactional(readOnly = true)
    public TaskStatistics getTaskStatistics(Long projectId) {
        long total = taskRepository.countByProjectId(projectId);
        long toDo = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.TO_DO);
        long inProgress = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.IN_PROGRESS);
        long done = taskRepository.countByProjectIdAndStatus(projectId, TaskStatus.DONE);
        
        return new TaskStatistics(total, toDo, inProgress, done);
    }
    
    private static String blankToNull(String s) {
        return s != null && !s.trim().isEmpty() ? s.trim() : null;
    }

    private static LocalDateTime parseDueDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String v = value.trim().replace("Z", "").replace("z", "");
        if (v.length() > 19) v = v.substring(0, 19);
        try {
            return LocalDateTime.parse(v);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Convert Task entity to DTO
     */
    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setProjectId(task.getProjectId());
        dto.setAssignedTo(task.getAssignedTo());
        dto.setCreatedBy(task.getCreatedBy());
        dto.setDueDate(task.getDueDate());
        dto.setCompletedAt(task.getCompletedAt());
        dto.setPriority(task.getPriority());
        dto.setOrderIndex(task.getOrderIndex());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        // Note: assignedToName and createdByName should be populated by calling user-service
        return dto;
    }
    
    /**
     * Task statistics inner class
     */
    public static class TaskStatistics {
        private long total;
        private long toDo;
        private long inProgress;
        private long done;
        
        public TaskStatistics(long total, long toDo, long inProgress, long done) {
            this.total = total;
            this.toDo = toDo;
            this.inProgress = inProgress;
            this.done = done;
        }
        
        // Getters
        public long getTotal() { return total; }
        public long getToDo() { return toDo; }
        public long getInProgress() { return inProgress; }
        public long getDone() { return done; }
        
        public double getCompletionRate() {
            return total > 0 ? (done * 100.0 / total) : 0.0;
        }
    }
}
