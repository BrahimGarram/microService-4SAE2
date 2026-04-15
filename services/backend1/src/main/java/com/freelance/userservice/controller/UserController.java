package com.freelance.userservice.controller;

import com.freelance.userservice.dto.CreateUserRequest;
import com.freelance.userservice.dto.DashboardStatsDTO;
import com.freelance.userservice.dto.ForgotPasswordRequest;
import com.freelance.userservice.dto.RegisterRequest;
import com.freelance.userservice.dto.ResetPasswordRequest;
import com.freelance.userservice.dto.SuggestFreelancersRequest;
import com.freelance.userservice.dto.UpdateUserRequest;
import com.freelance.userservice.dto.UserDTO;
import com.freelance.userservice.model.UserRole;
import com.freelance.userservice.service.KeycloakService;
import com.freelance.userservice.service.StatisticsService;
import com.freelance.userservice.service.UserService;
import com.freelance.userservice.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User REST Controller
 * 
 * Provides REST endpoints for user management.
 * All endpoints require JWT authentication from Keycloak.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    private final KeycloakService keycloakService;
    private final com.freelance.userservice.service.PasswordResetService passwordResetService;
    private final com.freelance.userservice.service.EmailService emailService;
    private final StatisticsService statisticsService;
    private final com.freelance.userservice.service.IpGeolocationService ipGeolocationService;
    private final com.freelance.userservice.service.FileStorageService fileStorageService;
    private final com.freelance.userservice.service.RatingService ratingService;
    private final com.freelance.userservice.service.FreelancerSuggestService freelancerSuggestService;

    /**
     * POST /api/users/ai/suggest-freelancers
     * Suggest freelancers based on natural language (Ollama).
     */
    @PostMapping("/ai/suggest-freelancers")
    public ResponseEntity<List<UserDTO>> suggestFreelancers(@Valid @RequestBody SuggestFreelancersRequest request) {
        List<UserDTO> suggested = freelancerSuggestService.suggestFreelancers(request.getMessage());
        return ResponseEntity.ok(suggested);
    }

    /**
     * PUT /api/users/internal/{userId}/token-balance
     * Internal: called by payment-service to sync wallet balance to user. No auth required.
     */
    @PutMapping("/internal/{userId}/token-balance")
    public ResponseEntity<UserDTO> updateTokenBalance(@PathVariable Long userId, @RequestBody Map<String, Integer> body) {
        Integer balance = body != null ? body.get("tokenBalance") : null;
        if (balance == null) {
            return ResponseEntity.badRequest().build();
        }
        UserDTO updated = userService.updateTokenBalance(userId, balance);
        return ResponseEntity.ok(updated);
    }

    /**
     * GET /api/users/me
     *
     * Returns the currently authenticated user's profile.
     * Extracts user info from JWT token.
     * 
     * @param authentication Spring Security authentication object containing JWT
     * @return UserDTO of current user
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = JwtUtil.extractKeycloakId(jwt);
        
        UserDTO user = userService.getCurrentUser(keycloakId);
        return ResponseEntity.ok(user);
    }
    
    /**
     * GET /api/users
     * 
     * Returns all users in the system.
     * ADMIN role required.
     * 
     * @return List of all UserDTO
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * GET /api/users/public
     * 
     * Returns public profiles of all users.
     * Any authenticated user can access this endpoint.
     * Returns only public information (no sensitive data).
     * 
     * @return List of public UserDTO (limited fields)
     */
    @GetMapping("/public")
    public ResponseEntity<List<UserDTO>> getPublicUsers() {
        List<UserDTO> users = userService.getPublicUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * GET /api/users/{id}
     * 
     * Returns a specific user's public profile by ID.
     * Public endpoint - any authenticated user can view other users' profiles.
     * 
     * @param id User ID
     * @return UserDTO with public profile information
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * GET /api/users/{id}/followers
     * Returns the list of users who follow this user.
     */
    @GetMapping("/{id}/followers")
    public ResponseEntity<List<UserDTO>> getFollowers(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getFollowers(id));
    }

    /**
     * GET /api/users/{id}/following
     * Returns the list of users that this user follows.
     */
    @GetMapping("/{id}/following")
    public ResponseEntity<List<UserDTO>> getFollowing(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getFollowing(id));
    }

    /**
     * GET /api/users/internal/{id}
     *
     * Internal endpoint for other microservices (e.g. service-test via OpenFeign).
     * Returns user id and username so that service-test can display freelancer names in affectations.
     * Security: permitAll in SecurityConfig so OpenFeign can call without JWT.
     *
     * @param id User ID
     * @return UserDTO (id, username used by UserSummaryDto in service-test)
     */
    @GetMapping("/internal/{id}")
    public ResponseEntity<UserDTO> getUserByIdInternal(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    /**
     * GET /api/users/username/{username}
     * 
     * Returns a specific user's public profile by username.
     * Public endpoint - any authenticated user can view other users' profiles.
     * 
     * @param username Username
     * @return UserDTO with public profile information
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }
    
    /**
     * GET /api/users/stats
     * 
     * Returns dashboard statistics.
     * ADMIN role required.
     * 
     * @return DashboardStatsDTO with all statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = statisticsService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * POST /api/users/sync
     * 
     * Syncs user from Keycloak to database.
     * Called after first login to create user profile in database.
     * Can be called without authentication (permitAll) or with authentication.
     * 
     * @param authentication Spring Security authentication object
     * @param request HttpServletRequest to extract client IP
     * @return UserDTO of synced user
     */
    @PostMapping("/sync")
    public ResponseEntity<UserDTO> syncUser(Authentication authentication, HttpServletRequest request) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        // Extract user info from JWT
        String keycloakId = JwtUtil.extractKeycloakId(jwt);
        String username = JwtUtil.extractUsername(jwt);
        String email = JwtUtil.extractEmail(jwt);
        UserRole role = JwtUtil.extractUserRole(jwt);
        
        // Extract client IP for geolocation
        String clientIp = request.getRemoteAddr();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String ipAddress = ipGeolocationService.extractIpAddress(clientIp, forwardedFor);
        
        log.info("Syncing user - Client IP: {}, Forwarded-For: {}, Extracted IP: {}", 
                clientIp, forwardedFor, ipAddress);
        
        // Create sync request
        CreateUserRequest createRequest = new CreateUserRequest(
            keycloakId,
            username != null ? username : keycloakId,
            email != null ? email : "",
            role,
            null // Country will be detected from IP
        );
        
        UserDTO user = userService.syncUser(createRequest, ipAddress);
        
        // Log the result
        log.info("User synced successfully - Username: {}, Country: {}", 
                user.getUsername(), user.getCountry());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    /**
     * PUT /api/users/{id}
     * 
     * Updates user profile.
     * Users can only update their own profile unless they are ADMIN.
     * 
     * @param id User ID to update
     * @param request UpdateUserRequest with fields to update
     * @param authentication Spring Security authentication object
     * @return Updated UserDTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserKeycloakId = JwtUtil.extractKeycloakId(jwt);
        
        // Get current user to check permissions
        UserDTO currentUser = userService.getCurrentUser(currentUserKeycloakId);
        
        // Check if user is updating their own profile or is ADMIN
        if (!currentUser.getId().equals(id) && !JwtUtil.extractRoles(jwt).contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        UserDTO updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }
    
    /**
     * POST /api/users/{id}/profile-picture
     * 
     * Uploads a profile picture for the user.
     * Users can only update their own profile picture unless they are ADMIN.
     * 
     * @param id User ID
     * @param file MultipartFile containing the image
     * @param authentication Spring Security authentication object
     * @return Updated UserDTO with new profile picture URL
     */
    @PostMapping("/{id}/profile-picture")
    public ResponseEntity<UserDTO> uploadProfilePicture(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserKeycloakId = JwtUtil.extractKeycloakId(jwt);
        
        // Get current user to check permissions
        UserDTO currentUser = userService.getCurrentUser(currentUserKeycloakId);
        
        // Check if user is updating their own profile or is ADMIN
        if (!currentUser.getId().equals(id) && !JwtUtil.extractRoles(jwt).contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            // Get user entity
            com.freelance.userservice.model.User user = userService.getUserEntityById(id);
            
            // Delete old profile picture if exists
            if (user.getProfilePicture() != null && !user.getProfilePicture().isBlank()) {
                fileStorageService.deleteProfilePicture(user.getProfilePicture(), id);
            }
            
            // Upload new profile picture
            String profilePictureUrl = fileStorageService.uploadProfilePicture(file, id);
            
            // Update user with new profile picture URL
            user.setProfilePicture(profilePictureUrl);
            userService.saveUser(user);
            
            UserDTO updatedUser = userService.getUserById(id);
            return ResponseEntity.ok(updatedUser);
        } catch (IOException e) {
            log.error("Error uploading profile picture for user {}: {}", id, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload profile picture: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (IllegalArgumentException e) {
            log.error("Invalid file for user {}: {}", id, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    /**
     * POST /api/users/register
     * 
     * Registers a new user in Keycloak and creates user profile in database.
     * Public endpoint (no authentication required).
     * 
     * @param request RegisterRequest with user registration data
     * @param httpRequest HttpServletRequest to extract client IP
     * @return UserDTO of created user
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            // Register user in Keycloak
            String keycloakId = keycloakService.registerUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
            );
            
            // Create user in database
            UserRole role = UserRole.valueOf(request.getRole().toUpperCase());
            
            // Extract client IP for geolocation
            String forwardedFor = httpRequest != null ? httpRequest.getHeader("X-Forwarded-For") : null;
            String clientIp = httpRequest != null ? httpRequest.getRemoteAddr() : null;
            String ipAddress = ipGeolocationService.extractIpAddress(clientIp, forwardedFor);
            
            log.info("Registering user - Client IP: {}, Forwarded-For: {}, Extracted IP: {}", 
                    clientIp, forwardedFor, ipAddress);
            
            CreateUserRequest createRequest = new CreateUserRequest(
                keycloakId,
                request.getUsername(),
                request.getEmail(),
                role,
                null // Country will be detected from IP
            );
            
            UserDTO user = userService.syncUser(createRequest, ipAddress);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(null); // Error will be handled by exception handler
        }
    }
    
    /**
     * DELETE /api/users/{id}
     * 
     * Deletes a user from the system.
     * ADMIN role required.
     * 
     * @param id User ID to delete
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * POST /api/users/update-countries
     * 
     * Updates country for all users that have "Unknown" or null country.
     * ADMIN role required.
     * 
     * @param requestBody Optional body with "testIp" field to use a specific IP for testing
     * @param httpRequest HttpServletRequest to extract client IP
     * @return Map with count of updated users
     */
    @PostMapping("/update-countries")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateCountries(
            @RequestBody(required = false) Map<String, String> requestBody,
            HttpServletRequest httpRequest) {
        
        String ipAddress;
        
        // If test IP is provided in request body, use it
        if (requestBody != null && requestBody.containsKey("testIp")) {
            ipAddress = requestBody.get("testIp");
            log.info("Using test IP from request body: {}", ipAddress);
        } else {
            // Extract IP from current request
            String clientIp = httpRequest.getRemoteAddr();
            String forwardedFor = httpRequest.getHeader("X-Forwarded-For");
            ipAddress = ipGeolocationService.extractIpAddress(clientIp, forwardedFor);
            log.info("Extracted IP from request - Client IP: {}, Forwarded-For: {}, Extracted: {}", 
                    clientIp, forwardedFor, ipAddress);
        }
        
        int updatedCount = userService.updateCountriesForUnknownUsers(ipAddress);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Updated countries for " + updatedCount + " users");
        response.put("updatedCount", updatedCount);
        response.put("ipUsed", ipAddress);
        
        if (ipAddress.equals("127.0.0.1") || ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.")) {
            response.put("warning", "You're using a private IP. For real country detection, use a public IP. " +
                    "You can provide a test IP in the request body: {\"testIp\": \"8.8.8.8\"}");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/users/forgot-password
     * 
     * Generates a password reset token and sends email.
     * Public endpoint (no authentication required).
     * 
     * @param request ForgotPasswordRequest with user email
     * @return Success message with reset token (for development) or success message
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            // Generate reset token
            String token = passwordResetService.createResetToken(request.getEmail());
            
            // Send custom email with reset link
            try {
                emailService.sendPasswordResetEmail(request.getEmail(), token);
                log.info("Password reset email sent to: {}", request.getEmail());
            } catch (Exception e) {
                log.warn("Failed to send email, but token was generated. Email: {}", request.getEmail(), e);
                // Continue - token is still valid and can be used
            }
            
            // Always return success message to avoid revealing if user exists
            Map<String, String> response = new HashMap<>();
            response.put("message", "If an account with that email exists, a password reset link has been sent.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Still return success for security reasons
            Map<String, String> response = new HashMap<>();
            response.put("message", "If an account with that email exists, a password reset link has been sent.");
            return ResponseEntity.ok(response);
        }
    }
    
    /**
     * POST /api/users/reset-password
     * 
     * Resets user password using a reset token.
     * Public endpoint (no authentication required).
     * 
     * @param request ResetPasswordRequest with token and new password
     * @return Success or error message
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            boolean success = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            
            if (success) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Password reset successfully. You can now login with your new password.");
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Invalid or expired token");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("Error resetting password", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to reset password. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * POST /api/users/{id}/ratings
     * 
     * Create or update a rating for a user.
     * Users can rate other users (not themselves).
     * 
     * @param id User ID being rated
     * @param request CreateRatingRequest with rating and comment
     * @param authentication Spring Security authentication object
     * @return Created/Updated RatingDTO
     */
    @PostMapping("/{id}/ratings")
    public ResponseEntity<com.freelance.userservice.dto.RatingDTO> createRating(
            @PathVariable Long id,
            @Valid @RequestBody com.freelance.userservice.dto.CreateRatingRequest request,
            Authentication authentication) {
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserKeycloakId = JwtUtil.extractKeycloakId(jwt);
        UserDTO currentUser = userService.getCurrentUser(currentUserKeycloakId);
        
        com.freelance.userservice.dto.RatingDTO rating = ratingService.createOrUpdateRating(
            currentUser.getId(), id, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(rating);
    }
    
    /**
     * GET /api/users/{id}/ratings
     * 
     * Get all ratings for a user.
     * 
     * @param id User ID
     * @return List of RatingDTO
     */
    @GetMapping("/{id}/ratings")
    public ResponseEntity<List<com.freelance.userservice.dto.RatingDTO>> getUserRatings(@PathVariable Long id) {
        List<com.freelance.userservice.dto.RatingDTO> ratings = ratingService.getRatingsForUser(id);
        return ResponseEntity.ok(ratings);
    }
    
    /**
     * DELETE /api/users/ratings/{ratingId}
     * 
     * Delete a rating.
     * Users can only delete their own ratings.
     * 
     * @param ratingId Rating ID
     * @param authentication Spring Security authentication object
     * @return No content response
     */
    @DeleteMapping("/ratings/{ratingId}")
    public ResponseEntity<Void> deleteRating(
            @PathVariable Long ratingId,
            Authentication authentication) {
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserKeycloakId = JwtUtil.extractKeycloakId(jwt);
        UserDTO currentUser = userService.getCurrentUser(currentUserKeycloakId);
        
        ratingService.deleteRating(ratingId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
