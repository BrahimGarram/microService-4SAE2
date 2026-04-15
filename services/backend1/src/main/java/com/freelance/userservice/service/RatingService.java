package com.freelance.userservice.service;

import com.freelance.userservice.dto.CreateRatingRequest;
import com.freelance.userservice.dto.RatingDTO;
import com.freelance.userservice.exception.ResourceNotFoundException;
import com.freelance.userservice.model.Rating;
import com.freelance.userservice.model.User;
import com.freelance.userservice.repository.RatingRepository;
import com.freelance.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Rating Service
 * Handles user ratings and reviews
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RatingService {
    
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    
    /**
     * Create or update a rating
     * If user already rated, update the existing rating
     * @param raterId ID of user giving the rating
     * @param ratedUserId ID of user being rated
     * @param request Rating request with rating value and comment
     * @return RatingDTO
     */
    public RatingDTO createOrUpdateRating(Long raterId, Long ratedUserId, CreateRatingRequest request) {
        // Prevent self-rating
        if (raterId.equals(ratedUserId)) {
            throw new IllegalArgumentException("You cannot rate yourself");
        }
        
        User rater = userRepository.findById(raterId)
            .orElseThrow(() -> new ResourceNotFoundException("Rater not found with id: " + raterId));
        
        User ratedUser = userRepository.findById(ratedUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + ratedUserId));
        
        // Check if rating already exists
        Rating rating = ratingRepository.findByRaterAndRatedUser(rater, ratedUser)
            .orElse(new Rating());
        
        // Update or set rating properties
        rating.setRater(rater);
        rating.setRatedUser(ratedUser);
        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        
        Rating savedRating = ratingRepository.save(rating);
        log.info("Rating {} by user {} for user {}", savedRating.getRating(), raterId, ratedUserId);
        
        return convertToDTO(savedRating);
    }
    
    /**
     * Get all ratings for a user
     * @param userId User ID
     * @return List of RatingDTO
     */
    @Transactional(readOnly = true)
    public List<RatingDTO> getRatingsForUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<Rating> ratings = ratingRepository.findByRatedUserOrderByCreatedAtDesc(user);
        return ratings.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get average rating for a user
     * @param userId User ID
     * @return Average rating (0.0 to 5.0)
     */
    @Transactional(readOnly = true)
    public Double getAverageRating(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Double average = ratingRepository.calculateAverageRating(user);
        return average != null ? average : 0.0;
    }
    
    /**
     * Get total rating count for a user
     * @param userId User ID
     * @return Total number of ratings
     */
    @Transactional(readOnly = true)
    public long getRatingCount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return ratingRepository.countByRatedUser(user);
    }
    
    /**
     * Check if user has already rated another user
     * @param raterId ID of user giving the rating
     * @param ratedUserId ID of user being rated
     * @return true if already rated
     */
    @Transactional(readOnly = true)
    public boolean hasUserRated(Long raterId, Long ratedUserId) {
        User rater = userRepository.findById(raterId)
            .orElseThrow(() -> new ResourceNotFoundException("Rater not found with id: " + raterId));
        
        User ratedUser = userRepository.findById(ratedUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + ratedUserId));
        
        return ratingRepository.existsByRaterAndRatedUser(rater, ratedUser);
    }
    
    /**
     * Delete a rating
     * @param ratingId Rating ID
     * @param userId User ID (must be the rater)
     */
    public void deleteRating(Long ratingId, Long userId) {
        Rating rating = ratingRepository.findById(ratingId)
            .orElseThrow(() -> new ResourceNotFoundException("Rating not found with id: " + ratingId));
        
        if (!rating.getRater().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own ratings");
        }
        
        ratingRepository.delete(rating);
        log.info("Rating {} deleted by user {}", ratingId, userId);
    }
    
    /**
     * Convert Rating entity to RatingDTO
     */
    private RatingDTO convertToDTO(Rating rating) {
        return new RatingDTO(
            rating.getId(),
            rating.getRater().getId(),
            rating.getRater().getUsername(),
            rating.getRatedUser().getId(),
            rating.getRating(),
            rating.getComment(),
            rating.getCreatedAt(),
            rating.getUpdatedAt()
        );
    }
}
