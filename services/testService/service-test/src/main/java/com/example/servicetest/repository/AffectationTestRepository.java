package com.example.servicetest.repository;
import com.example.servicetest.entity.AffectationTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AffectationTestRepository extends JpaRepository<AffectationTest, Long> {
    long countByFreelancerId(Long freelancerId);

    Optional<AffectationTest> findByFreelancerId(Long id);

    java.util.List<AffectationTest> findAllByFreelancerIdOrderByAssignedAtDesc(Long freelancerId);

    /** Charge l'affectation avec ses questions (évite lazy loading et "0 questions" au front). */
    @Query("SELECT a FROM AffectationTest a LEFT JOIN FETCH a.questions WHERE a.id = :id")
    Optional<AffectationTest> findByIdWithQuestions(@Param("id") Long id);
}
