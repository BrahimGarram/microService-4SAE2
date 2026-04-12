package com.freelance.userservice.repository;

import com.freelance.userservice.model.Rating;
import com.freelance.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    /**
     * Find all ratings for a specific user
     */
    List<Rating> findByRatedUserOrderByCreatedAtDesc(User ratedUser);
    
    /**
     * Find rating by rater and rated user (to check if user already rated)
     */
    Optional<Rating> findByRaterAndRatedUser(User rater, User ratedUser);
    
    /**
     * Calculate average rating for a user
     */
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.ratedUser = :user")
    Double calculateAverageRating(@Param("user") User user);
    
    /**
     * Count total ratings for a user
     */
    long countByRatedUser(User ratedUser);
    
    /**
     * Check if user has already rated another user
     */
    boolean existsByRaterAndRatedUser(User rater, User ratedUser);
}
