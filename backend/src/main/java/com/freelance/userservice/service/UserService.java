package com.freelance.userservice.service;

import com.freelance.userservice.dto.*;
import com.freelance.userservice.exception.ResourceNotFoundException;
import com.freelance.userservice.model.*;
import com.freelance.userservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User Service Layer
 * 
 * Contains business logic for user operations.
 * Handles user creation, updates, and retrieval.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final IpGeolocationService ipGeolocationService;
    private final SkillRepository skillRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final LanguageRepository languageRepository;
    private final SocialLinkRepository socialLinkRepository;
    private final com.freelance.userservice.repository.RatingRepository ratingRepository;
    
    /**
     * Get current authenticated user by Keycloak ID
     * @param keycloakId The Keycloak user ID (sub claim)
     * @return UserDTO with all profile data
     * @throws ResourceNotFoundException if user not found
     */
    public UserDTO getCurrentUser(String keycloakId) {
        User user = userRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with keycloakId: " + keycloakId));
        return convertToDTO(user);
    }
    
    /**
     * Get all users (Admin only)
     * @return List of UserDTO
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get public users (for browsing/searching)
     * Returns only public information - excludes keycloakId (sensitive authentication data)
     * Includes: username, email, bio, portfolio, skills, languages, social links
     * @return List of UserDTO with public fields only
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getPublicUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
            .map(this::convertToPublicDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert User entity to UserDTO with only public fields
     * Includes: username, email, bio, portfolio, skills, languages, social links
     * Excludes: keycloakId (sensitive authentication data)
     */
    private UserDTO convertToPublicDTO(User user) {
        UserDTO.UserDTOBuilder builder = UserDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail()) // Email is public information
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .country(user.getCountry())
            .profilePicture(user.getProfilePicture())
            .bio(user.getBio())
            .city(user.getCity())
            .timezone(user.getTimezone())
            .hourlyRate(user.getHourlyRate())
            .availability(user.getAvailability())
            .verified(user.getVerified());
        
        // Load related entities (public information)
        List<Skill> skills = skillRepository.findByUser(user);
        builder.skills(skills.stream()
            .map(s -> new SkillDTO(s.getId(), s.getName(), s.getLevel(), s.getYearsOfExperience()))
            .collect(Collectors.toList()));
        
        List<PortfolioItem> portfolioItems = portfolioItemRepository.findByUserOrderByDisplayOrderAsc(user);
        builder.portfolioItems(portfolioItems.stream()
            .map(p -> new PortfolioItemDTO(p.getId(), p.getTitle(), p.getDescription(), 
                p.getUrl(), p.getImageUrl(), p.getTechnologies(), p.getCompletedDate(), p.getDisplayOrder()))
            .collect(Collectors.toList()));
        
        List<Language> languages = languageRepository.findByUser(user);
        builder.languages(languages.stream()
            .map(l -> new LanguageDTO(l.getId(), l.getName(), l.getCode(), l.getProficiency()))
            .collect(Collectors.toList()));
        
        List<SocialLink> socialLinks = socialLinkRepository.findByUser(user);
        builder.socialLinks(socialLinks.stream()
            .map(s -> new SocialLinkDTO(s.getId(), s.getPlatform(), s.getUrl(), s.getUsername()))
            .collect(Collectors.toList()));
        
        // Add rating information
        Double averageRating = ratingRepository.calculateAverageRating(user);
        long ratingCount = ratingRepository.countByRatedUser(user);
        builder.averageRating(averageRating != null ? averageRating : 0.0);
        builder.ratingCount(ratingCount);
        
        // Note: keycloakId is NOT included in public DTO (sensitive authentication data)
        return builder.build();
    }
    
    /**
     * Sync user from Keycloak to database
     * Creates user if doesn't exist, updates if exists
     * @param request CreateUserRequest with user data from Keycloak
     * @param clientIp Client IP address for geolocation (optional)
     * @return UserDTO
     */
    public UserDTO syncUser(CreateUserRequest request, String clientIp) {
        User user = userRepository.findByKeycloakId(request.getKeycloakId())
            .orElse(new User());
        
        // Update or set user properties
        user.setKeycloakId(request.getKeycloakId());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        
        // Ensure required fields have default values if not set
        // These are handled by @PrePersist, but set explicitly for safety
        if (user.getAvailability() == null) {
            user.setAvailability(UserAvailability.OFFLINE);
        }
        if (user.getVerified() == null) {
            user.setVerified(false);
        }
        
        // Set country if provided, otherwise detect from IP
        if (request.getCountry() != null && !request.getCountry().isBlank()) {
            user.setCountry(request.getCountry());
            log.info("Country set from request for user {}: {}", request.getUsername(), request.getCountry());
        } else if (clientIp != null && !clientIp.isBlank()) {
            // Always detect country from IP if:
            // 1. User doesn't have a country yet (null)
            // 2. OR user has "Unknown" country (to update it with real IP)
            boolean shouldDetectCountry = user.getCountry() == null || "Unknown".equals(user.getCountry());
            
            if (shouldDetectCountry) {
                String country = ipGeolocationService.getCountryFromIp(clientIp);
                if (country != null && !country.isBlank()) {
                    user.setCountry(country);
                    log.info("Detected country '{}' for user {} from IP {}", country, request.getUsername(), clientIp);
                } else {
                    log.warn("Could not detect country for IP {} (returned null or blank). User: {}", clientIp, request.getUsername());
                }
            } else {
                log.debug("User {} already has country '{}', skipping IP detection. IP: {}", 
                        request.getUsername(), user.getCountry(), clientIp);
            }
        } else {
            log.warn("No client IP provided for user {}, cannot detect country", request.getUsername());
        }
        
        try {
            User savedUser = userRepository.save(user);
            log.info("User synced successfully - ID: {}, Username: {}, Email: {}, Role: {}", 
                    savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getRole());
            return convertToDTO(savedUser);
        } catch (Exception e) {
            log.error("Error saving user to database - Username: {}, Email: {}, Error: {}", 
                    request.getUsername(), request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Sync user from Keycloak to database (overload without IP)
     * @param request CreateUserRequest with user data from Keycloak
     * @return UserDTO
     */
    public UserDTO syncUser(CreateUserRequest request) {
        return syncUser(request, null);
    }
    
    /**
     * Update user profile
     * Updates both MySQL database and Keycloak
     * @param id User ID
     * @param request UpdateUserRequest with fields to update
     * @return UserDTO
     * @throws ResourceNotFoundException if user not found
     */
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Store old values for Keycloak update
        String oldUsername = user.getUsername();
        String oldEmail = user.getEmail();
        String keycloakId = user.getKeycloakId();
        
        // Track what changed (trim and normalize for comparison)
        boolean usernameChanged = false;
        boolean emailChanged = false;
        
        // Update only provided fields
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            String newUsername = request.getUsername().trim();
            if (!newUsername.equals(oldUsername)) {
                user.setUsername(newUsername);
                usernameChanged = true;
                log.info("Username changed from '{}' to '{}' for user ID: {}", oldUsername, newUsername, id);
            }
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(oldEmail)) {
                user.setEmail(newEmail);
                emailChanged = true;
                log.info("Email changed from '{}' to '{}' for user ID: {}", oldEmail, newEmail, id);
            }
        }
        
        // Update profile fields
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }
        if (request.getTimezone() != null) {
            user.setTimezone(request.getTimezone());
        }
        if (request.getHourlyRate() != null) {
            user.setHourlyRate(request.getHourlyRate());
        }
        if (request.getAvailability() != null) {
            user.setAvailability(request.getAvailability());
        }
        
        // Update related entities if provided
        if (request.getSkills() != null) {
            updateUserSkills(user, request.getSkills());
        }
        if (request.getPortfolioItems() != null) {
            updateUserPortfolio(user, request.getPortfolioItems());
        }
        if (request.getLanguages() != null) {
            updateUserLanguages(user, request.getLanguages());
        }
        if (request.getSocialLinks() != null) {
            updateUserSocialLinks(user, request.getSocialLinks());
        }
        
        // Save to database first
        User updatedUser = userRepository.save(user);
        log.info("User {} updated in database", id);
        
        // Update Keycloak if username or email changed
        if (keycloakId == null || keycloakId.isBlank()) {
            log.warn("Keycloak ID is null or blank for user ID: {}, skipping Keycloak update", id);
        } else if (usernameChanged || emailChanged) {
            try {
                // Always send the updated values to Keycloak
                String newUsername = usernameChanged ? updatedUser.getUsername() : null;
                String newEmail = emailChanged ? updatedUser.getEmail() : null;
                
                log.info("Updating Keycloak user {} - Username: {}, Email: {}", keycloakId, newUsername, newEmail);
                keycloakService.updateUser(keycloakId, newUsername, newEmail);
                log.info("User {} updated successfully in both database and Keycloak", id);
            } catch (Exception e) {
                log.error("Failed to update user in Keycloak for user ID: {} (Keycloak ID: {})", id, keycloakId, e);
                log.error("Exception message: {}", e.getMessage());
                // Note: We don't throw here because the database update succeeded
                // The user will see the update in the database, but Keycloak might be out of sync
                // In production, you might want to implement a retry mechanism or queue
            }
        } else {
            log.debug("No changes detected for user ID: {}, skipping Keycloak update", id);
        }
        
        return convertToDTO(updatedUser);
    }
    
    /**
     * Delete user (Admin only)
     * Deletes user from both MySQL database and Keycloak
     * @param id User ID
     * @throws ResourceNotFoundException if user not found
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        String keycloakId = user.getKeycloakId();
        
        // Delete from Keycloak first (if keycloakId exists)
        if (keycloakId != null && !keycloakId.isBlank()) {
            try {
                keycloakService.deleteUser(keycloakId);
                log.info("User {} deleted from Keycloak, proceeding with database deletion", id);
            } catch (Exception e) {
                log.error("Failed to delete user from Keycloak for user ID: {}", id, e);
                // Note: We still proceed with database deletion even if Keycloak deletion fails
                // In production, you might want to implement a retry mechanism or queue
            }
        }
        
        // Delete from database
        userRepository.deleteById(id);
        log.info("User {} deleted successfully from database", id);
    }
    
    /**
     * Get user by ID
     * @param id User ID
     * @return UserDTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDTO(user);
    }
    
    /**
     * Get user entity by ID (for internal use)
     * @param id User ID
     * @return User entity
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
    
    /**
     * Get user by username
     * @param username Username
     * @return UserDTO with all profile data
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return convertToDTO(user);
    }
    
    /**
     * Save user entity (for internal use)
     * @param user User entity to save
     * @return Saved user entity
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    /**
     * Update countries for users with "Unknown" or null country
     * Note: This only works if we have a valid public IP to detect from.
     * For existing users created before country tracking, this won't work
     * unless we stored their original IP (which we don't).
     * 
     * @param testIp Optional IP address to use for testing (if provided, will use this for all users)
     * @return Number of users updated
     */
    public int updateCountriesForUnknownUsers(String testIp) {
        List<User> usersWithUnknownCountry = userRepository.findAll().stream()
            .filter(u -> u.getCountry() == null || "Unknown".equals(u.getCountry()))
            .collect(java.util.stream.Collectors.toList());
        
        if (usersWithUnknownCountry.isEmpty()) {
            log.info("No users with unknown country found");
            return 0;
        }
        
        int updatedCount = 0;
        
        // If test IP is provided and is not localhost/private, use it for all users
        // Otherwise, we can't update existing users without their original IP
        if (testIp != null && !testIp.isBlank() && 
            !testIp.equals("127.0.0.1") && !testIp.equals("Unknown") &&
            !testIp.startsWith("192.168.") && !testIp.startsWith("10.")) {
            
            String country = ipGeolocationService.getCountryFromIp(testIp);
            if (!"Unknown".equals(country)) {
                for (User user : usersWithUnknownCountry) {
                    user.setCountry(country);
                    userRepository.save(user);
                    updatedCount++;
                    log.info("Updated country for user {} to {}", user.getUsername(), country);
                }
            }
        } else {
            log.warn("Cannot update countries for existing users without valid public IP. " +
                    "Users created before country tracking was added will remain 'Unknown'. " +
                    "New registrations will automatically detect country.");
        }
        
        return updatedCount;
    }
    
    /**
     * Convert User entity to UserDTO with all related entities
     */
    private UserDTO convertToDTO(User user) {
        UserDTO.UserDTOBuilder builder = UserDTO.builder()
            .id(user.getId())
            .keycloakId(user.getKeycloakId())
            .username(user.getUsername())
            .email(user.getEmail())
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .country(user.getCountry())
            .profilePicture(user.getProfilePicture())
            .bio(user.getBio())
            .city(user.getCity())
            .timezone(user.getTimezone())
            .hourlyRate(user.getHourlyRate())
            .availability(user.getAvailability())
            .verified(user.getVerified());
        
        // Load related entities
        List<Skill> skills = skillRepository.findByUser(user);
        builder.skills(skills.stream()
            .map(s -> new SkillDTO(s.getId(), s.getName(), s.getLevel(), s.getYearsOfExperience()))
            .collect(Collectors.toList()));
        
        List<PortfolioItem> portfolioItems = portfolioItemRepository.findByUserOrderByDisplayOrderAsc(user);
        builder.portfolioItems(portfolioItems.stream()
            .map(p -> new PortfolioItemDTO(p.getId(), p.getTitle(), p.getDescription(), 
                p.getUrl(), p.getImageUrl(), p.getTechnologies(), p.getCompletedDate(), p.getDisplayOrder()))
            .collect(Collectors.toList()));
        
        List<Language> languages = languageRepository.findByUser(user);
        builder.languages(languages.stream()
            .map(l -> new LanguageDTO(l.getId(), l.getName(), l.getCode(), l.getProficiency()))
            .collect(Collectors.toList()));
        
        List<SocialLink> socialLinks = socialLinkRepository.findByUser(user);
        builder.socialLinks(socialLinks.stream()
            .map(s -> new SocialLinkDTO(s.getId(), s.getPlatform(), s.getUrl(), s.getUsername()))
            .collect(Collectors.toList()));
        
        return builder.build();
    }
    
    /**
     * Update user skills
     */
    private void updateUserSkills(User user, List<SkillDTO> skillDTOs) {
        // Delete existing skills
        skillRepository.deleteByUser(user);
        
        // Add new skills
        for (SkillDTO skillDTO : skillDTOs) {
            Skill skill = new Skill();
            skill.setUser(user);
            skill.setName(skillDTO.getName());
            skill.setLevel(skillDTO.getLevel());
            skill.setYearsOfExperience(skillDTO.getYearsOfExperience());
            skillRepository.save(skill);
        }
    }
    
    /**
     * Update user portfolio
     */
    private void updateUserPortfolio(User user, List<PortfolioItemDTO> portfolioDTOs) {
        // Delete existing portfolio items
        portfolioItemRepository.deleteByUser(user);
        
        // Add new portfolio items
        for (PortfolioItemDTO portfolioDTO : portfolioDTOs) {
            PortfolioItem item = new PortfolioItem();
            item.setUser(user);
            item.setTitle(portfolioDTO.getTitle());
            item.setDescription(portfolioDTO.getDescription());
            item.setUrl(portfolioDTO.getUrl());
            item.setImageUrl(portfolioDTO.getImageUrl());
            item.setTechnologies(portfolioDTO.getTechnologies());
            item.setCompletedDate(portfolioDTO.getCompletedDate());
            item.setDisplayOrder(portfolioDTO.getDisplayOrder());
            portfolioItemRepository.save(item);
        }
    }
    
    /**
     * Update user languages
     */
    private void updateUserLanguages(User user, List<LanguageDTO> languageDTOs) {
        // Delete existing languages
        languageRepository.deleteByUser(user);
        
        // Add new languages
        for (LanguageDTO languageDTO : languageDTOs) {
            Language language = new Language();
            language.setUser(user);
            language.setName(languageDTO.getName());
            language.setCode(languageDTO.getCode());
            language.setProficiency(languageDTO.getProficiency());
            languageRepository.save(language);
        }
    }
    
    /**
     * Update user social links
     */
    private void updateUserSocialLinks(User user, List<SocialLinkDTO> socialLinkDTOs) {
        // Delete existing social links
        socialLinkRepository.deleteByUser(user);
        
        // Add new social links
        for (SocialLinkDTO socialLinkDTO : socialLinkDTOs) {
            SocialLink link = new SocialLink();
            link.setUser(user);
            link.setPlatform(socialLinkDTO.getPlatform());
            link.setUrl(socialLinkDTO.getUrl());
            link.setUsername(socialLinkDTO.getUsername());
            socialLinkRepository.save(link);
        }
    }
}
