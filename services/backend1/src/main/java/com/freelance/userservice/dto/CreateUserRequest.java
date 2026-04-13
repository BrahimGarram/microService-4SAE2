package com.freelance.userservice.dto;

import com.freelance.userservice.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/syncing a user
 * 
 * Used when syncing user from Keycloak to database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    
    @NotBlank(message = "Keycloak ID is required")
    private String keycloakId;
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotNull(message = "Role is required")
    private UserRole role;
    
    /**
     * User's country (optional, will be detected from IP if not provided)
     */
    private String country;
}
