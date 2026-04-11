package com.freelancify.badge.repository;

import com.freelancify.badge.entity.FreelancerBadge;
import com.freelancify.badge.entity.FreelancerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FreelancerBadgeRepository extends JpaRepository<FreelancerBadge, Long> {
    List<FreelancerBadge> findByFreelancerProfile(FreelancerProfile profile);
    boolean existsByFreelancerProfileAndBadgeId(FreelancerProfile profile, Long badgeId);
}
