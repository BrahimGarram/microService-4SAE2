package com.freelance.userservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freelance.userservice.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Keycloak Service
 * 
 * Handles communication with Keycloak Admin REST API
 * for user registration and management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {
    
    @Value("${keycloak.admin.url}")
    private String keycloakAdminUrl;
    
    @Value("${keycloak.admin.username}")
    private String keycloakAdminUsername;
    
    @Value("${keycloak.admin.password}")
    private String keycloakAdminPassword;
    
    @Value("${keycloak.admin.realm}")
    private String realm;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Get admin access token from Keycloak
     */
    private String getAdminAccessToken() {
        try {
            String tokenUrl = keycloakAdminUrl + "/realms/master/protocol/openid-connect/token";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", "admin-cli");
            body.add("username", keycloakAdminUsername);
            body.add("password", keycloakAdminPassword);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            log.error("Error getting admin access token", e);
            throw new RuntimeException("Failed to get admin access token", e);
        }
    }
    
    /**
     * Register a new user in Keycloak
     * @param username Username
     * @param email Email
     * @param password Password
     * @param role Role (USER, FREELANCER, ADMIN)
     * @return Keycloak user ID
     */
    public String registerUser(String username, String email, String password, String role) {
        try {
            String adminToken = getAdminAccessToken();
            String usersUrl = keycloakAdminUrl + "/admin/realms/" + realm + "/users";
            
            // Create user
            Map<String, Object> userData = new HashMap<>();
            userData.put("username", username);
            userData.put("email", email);
            userData.put("emailVerified", true);
            userData.put("enabled", true);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);
            
            HttpEntity<Map<String, Object>> createUserRequest = new HttpEntity<>(userData, headers);
            ResponseEntity<String> createResponse = restTemplate.postForEntity(usersUrl, createUserRequest, String.class);
            
            if (createResponse.getStatusCode() != HttpStatus.CREATED) {
                throw new RuntimeException("Failed to create user in Keycloak: " + createResponse.getBody());
            }
            
            // Get user ID from Location header
            String location = createResponse.getHeaders().getFirst(HttpHeaders.LOCATION);
            String userId = location.substring(location.lastIndexOf('/') + 1);
            
            // Set password
            String passwordUrl = keycloakAdminUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";
            Map<String, Object> passwordData = new HashMap<>();
            passwordData.put("type", "password");
            passwordData.put("value", password);
            passwordData.put("temporary", false);
            
            HttpEntity<Map<String, Object>> passwordRequest = new HttpEntity<>(passwordData, headers);
            restTemplate.put(passwordUrl, passwordRequest);
            
            // Assign role
            String roleUrl = keycloakAdminUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
            
            // Get role ID
            String rolesUrl = keycloakAdminUrl + "/admin/realms/" + realm + "/roles/" + role;
            ResponseEntity<String> roleResponse = restTemplate.exchange(
                rolesUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
            );
            
            JsonNode roleNode = objectMapper.readTree(roleResponse.getBody());
            Map<String, Object> roleMapping = new HashMap<>();
            roleMapping.put("id", roleNode.get("id").asText());
            roleMapping.put("name", role);
            
            HttpEntity<Map<String, Object>[]> roleRequest = new HttpEntity<>(
                new Map[]{roleMapping},
                headers
            );
            restTemplate.postForEntity(roleUrl, roleRequest, String.class);
            
            log.info("User {} registered successfully in Keycloak with role {}", username, role);
            return userId;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                String msg = "Un utilisateur avec ce nom d'utilisateur ou cet email existe déjà.";
                try {
                    JsonNode body = objectMapper.readTree(e.getResponseBodyAsString());
                    if (body.has("errorMessage")) {
                        msg = body.get("errorMessage").asText();
                    }
                } catch (Exception ignored) {}
                log.warn("Registration conflict: {}", msg);
                throw new UserAlreadyExistsException(msg, e);
            }
            log.error("Error registering user in Keycloak", e);
            throw new RuntimeException("Failed to register user in Keycloak: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error registering user in Keycloak", e);
            throw new RuntimeException("Failed to register user in Keycloak: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update user in Keycloak
     * @param keycloakUserId Keycloak user ID
     * @param username New username (optional, null to skip)
     * @param email New email (optional, null to skip)
     */
    public void updateUser(String keycloakUserId, String username, String email) {
        try {
            String adminToken = getAdminAccessToken();
            String usersUrl = keycloakAdminUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);
            
            // Build update data
            Map<String, Object> updateData = new HashMap<>();
            if (username != null && !username.isBlank()) {
                updateData.put("username", username.trim());
                log.info("Will update username to: '{}' for Keycloak user: {}", username.trim(), keycloakUserId);
            }
            if (email != null && !email.isBlank()) {
                updateData.put("email", email.trim());
                updateData.put("emailVerified", true); // Mark email as verified after update
                log.info("Will update email to: '{}' for Keycloak user: {}", email.trim(), keycloakUserId);
            }
            
            // Only update if there's something to update
            if (!updateData.isEmpty()) {
                log.info("Updating Keycloak user {} with data: {}", keycloakUserId, updateData);
                HttpEntity<Map<String, Object>> updateRequest = new HttpEntity<>(updateData, headers);
                
                // Use exchange method for better control and error handling
                ResponseEntity<Void> response = restTemplate.exchange(
                    usersUrl,
                    HttpMethod.PUT,
                    updateRequest,
                    Void.class
                );
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("User {} updated successfully in Keycloak with status: {}", keycloakUserId, response.getStatusCode());
                } else {
                    log.warn("Keycloak update returned non-2xx status: {} for user: {}", response.getStatusCode(), keycloakUserId);
                }
            } else {
                log.warn("No data to update for Keycloak user: {}", keycloakUserId);
            }
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("HTTP client error updating user in Keycloak: {} - Status: {}, Body: {}", 
                keycloakUserId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to update user in Keycloak: " + e.getMessage(), e);
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            log.error("HTTP server error updating user in Keycloak: {} - Status: {}, Body: {}", 
                keycloakUserId, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Keycloak server error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error updating user in Keycloak: {}", keycloakUserId, e);
            throw new RuntimeException("Failed to update user in Keycloak: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete user from Keycloak
     * @param keycloakUserId Keycloak user ID
     */
    public void deleteUser(String keycloakUserId) {
        try {
            String adminToken = getAdminAccessToken();
            String usersUrl = keycloakAdminUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            
            HttpEntity<Void> deleteRequest = new HttpEntity<>(headers);
            restTemplate.exchange(usersUrl, HttpMethod.DELETE, deleteRequest, Void.class);
            
            log.info("User {} deleted successfully from Keycloak", keycloakUserId);
        } catch (Exception e) {
            log.error("Error deleting user from Keycloak: {}", keycloakUserId, e);
            throw new RuntimeException("Failed to delete user from Keycloak: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find user by email in Keycloak
     * @param email User email
     * @return Keycloak user ID if found, null otherwise
     */
    public String findUserByEmail(String email) {
        try {
            String adminToken = getAdminAccessToken();
            String usersUrl = keycloakAdminUrl + "/admin/realms/" + realm + "/users?email=" + email;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(adminToken);
            
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                usersUrl,
                HttpMethod.GET,
                request,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode users = objectMapper.readTree(response.getBody());
                if (users.isArray() && users.size() > 0) {
                    String userId = users.get(0).get("id").asText();
                    log.info("Found user with email {} in Keycloak: {}", email, userId);
                    return userId;
                }
            }
            
            log.warn("User with email {} not found in Keycloak", email);
            return null;
        } catch (Exception e) {
            log.error("Error finding user by email in Keycloak: {}", email, e);
            throw new RuntimeException("Failed to find user by email in Keycloak: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send password reset email to user (using Keycloak's built-in email)
     * @param email User email
     */
    public void sendPasswordResetEmail(String email) {
        try {
            // Find user by email
            String userId = findUserByEmail(email);
            if (userId == null) {
                // Don't reveal if user exists or not for security reasons
                log.info("Password reset requested for email: {} (user not found or email not verified)", email);
                return;
            }
            
            String adminToken = getAdminAccessToken();
            String executeActionsUrl = keycloakAdminUrl + "/admin/realms/" + realm + "/users/" + userId + "/execute-actions-email";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);
            
            // Keycloak requires an array of actions
            // UPDATE_PASSWORD will send a password reset email
            java.util.List<String> actions = new java.util.ArrayList<>();
            actions.add("UPDATE_PASSWORD");
            
            HttpEntity<java.util.List<String>> request = new HttpEntity<>(actions, headers);
            ResponseEntity<Void> response = restTemplate.exchange(
                executeActionsUrl,
                HttpMethod.PUT,
                request,
                Void.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Password reset email sent successfully to: {}", email);
            } else {
                log.warn("Failed to send password reset email. Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending password reset email to: {}", email, e);
            // Don't throw exception to avoid revealing if user exists
            // Just log the error
        }
    }
    
    /**
     * Reset user password directly using Keycloak Admin API
     * @param keycloakUserId Keycloak user ID
     * @param newPassword New password
     */
    public void resetPassword(String keycloakUserId, String newPassword) {
        try {
            String adminToken = getAdminAccessToken();
            String passwordUrl = keycloakAdminUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/reset-password";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(adminToken);
            
            Map<String, Object> passwordData = new HashMap<>();
            passwordData.put("type", "password");
            passwordData.put("value", newPassword);
            passwordData.put("temporary", false);
            
            HttpEntity<Map<String, Object>> passwordRequest = new HttpEntity<>(passwordData, headers);
            ResponseEntity<Void> response = restTemplate.exchange(
                passwordUrl,
                HttpMethod.PUT,
                passwordRequest,
                Void.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Password reset successfully for user: {}", keycloakUserId);
            } else {
                log.warn("Failed to reset password. Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to reset password in Keycloak");
            }
        } catch (Exception e) {
            log.error("Error resetting password for user: {}", keycloakUserId, e);
            throw new RuntimeException("Failed to reset password in Keycloak: " + e.getMessage(), e);
        }
    }
}
