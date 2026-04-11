package com.freelance.projectservice.dto;

import com.freelance.projectservice.model.ProjectStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for updating a project
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;
    
    @NotNull(message = "Status is required")
    private ProjectStatus status;
    
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;
    
    @NotNull(message = "Budget is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Budget must be positive or zero")
    @Digits(integer = 10, fraction = 2, message = "Budget must have at most 10 integer digits and 2 decimal places")
    private BigDecimal budget;
    
    @NotNull(message = "Deadline is required")
    private LocalDateTime deadline;
    
    @URL(message = "Image URL must be a valid URL")
    @Size(max = 500, message = "Image URL cannot exceed 500 characters")
    private String imageUrl;
    
    @Size(max = 500, message = "Tags cannot exceed 500 characters")
    private String tags;
}
