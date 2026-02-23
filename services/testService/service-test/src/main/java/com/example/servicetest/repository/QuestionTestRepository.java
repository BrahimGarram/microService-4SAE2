package com.example.servicetest.repository;
import com.example.servicetest.entity.QuestionTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionTestRepository extends JpaRepository<QuestionTest, Long> {
}