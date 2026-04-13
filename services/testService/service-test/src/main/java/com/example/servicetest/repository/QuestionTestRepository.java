package com.example.servicetest.repository;
import com.example.servicetest.entity.QuestionTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionTestRepository extends JpaRepository<QuestionTest, Long> {

    List<QuestionTest> findByIsActiveTrue();

    List<QuestionTest> findByDomainInAndIsActiveTrue(List<String> domains);

    /** Questions CODING actives (filtre testCasesJson non vide fait en Java). */
    @Query("SELECT q FROM QuestionTest q WHERE q.questionType = com.example.servicetest.entity.QuestionType.CODING AND (q.isActive = true)")
    List<QuestionTest> findActiveCodingQuestions();
}
