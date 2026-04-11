package com.freelancify.badge.repository;

import com.freelancify.badge.entity.FreelancerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FreelancerProfileRepository extends JpaRepository<FreelancerProfile, Long> {
    Optional<FreelancerProfile> findByFreelancerEmail(String email);

    @Query("SELECT f FROM FreelancerProfile f ORDER BY f.missionsCompleted DESC, f.averageRating DESC")
    List<FreelancerProfile> findLeaderboard();
}
