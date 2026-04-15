package com.freelance.userservice.repository;

import com.freelance.userservice.model.User;
import com.freelance.userservice.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Repository
 * 
 * Provides data access methods for User entity.
 * Spring Data JPA automatically implements this interface.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by Keycloak ID (sub claim from JWT)
     * @param keycloakId The Keycloak user ID
     * @return Optional User if found
     */
    Optional<User> findByKeycloakId(String keycloakId);
    
    /**
     * Find user by email
     * @param email The user email
     * @return Optional User if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by username
     * @param username The username
     * @return Optional User if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Check if user exists by Keycloak ID
     * @param keycloakId The Keycloak user ID
     * @return true if user exists
     */
    boolean existsByKeycloakId(String keycloakId);
    
    /**
     * Count users by role
     * @param role User role
     * @return Count of users with this role
     */
    long countByRole(UserRole role);
    
    /**
     * Count users created after a specific date
     * @param date The date to compare
     * @return Count of users created after this date
     */
    long countByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Count users created between two dates
     * @param startDate Start date
     * @param endDate End date
     * @return Count of users created in this period
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find users created after a specific date
     * @param date The date to compare
     * @return List of users created after this date
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Get user count grouped by month
     * Returns count of users created in each month
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', u.createdAt, '%Y-%m') as period, COUNT(u) as count " +
           "FROM User u " +
           "WHERE u.createdAt >= :startDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', u.createdAt, '%Y-%m') " +
           "ORDER BY period")
    List<Object[]> countUsersByMonth(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Get user count grouped by day (for last 7 days)
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', u.createdAt, '%Y-%m-%d') as period, COUNT(u) as count " +
           "FROM User u " +
           "WHERE u.createdAt >= :startDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', u.createdAt, '%Y-%m-%d') " +
           "ORDER BY period")
    List<Object[]> countUsersByDay(@Param("startDate") LocalDateTime startDate);
    
    /**
     * Get user count grouped by country
     * Returns top countries with user counts
     */
    @Query("SELECT u.country, COUNT(u) as count " +
           "FROM User u " +
           "WHERE u.country IS NOT NULL " +
           "GROUP BY u.country " +
           "ORDER BY count DESC")
    List<Object[]> countUsersByCountry();
}
