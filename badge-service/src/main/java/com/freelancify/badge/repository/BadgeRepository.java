package com.freelancify.badge.repository;

import com.freelancify.badge.entity.Badge;
import com.freelancify.badge.entity.BadgeLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findByActiveTrue();
    List<Badge> findByLevel(BadgeLevel level);
    List<Badge> findByMinMissionsCompletedLessThanEqualAndMinAverageRatingLessThanEqual(
            int missions, double rating);
}
