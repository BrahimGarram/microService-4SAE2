package com.example.servicetest.repository;
import com.example.servicetest.entity.AffectationTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AffectationTestRepository extends JpaRepository<AffectationTest, Long> {
    long countByFreelancerId(Long freelancerId);

    Optional<AffectationTest> findByFreelancerId(Long id);

    java.util.List<AffectationTest> findAllByFreelancerIdOrderByAssignedAtDesc(Long freelancerId);
}