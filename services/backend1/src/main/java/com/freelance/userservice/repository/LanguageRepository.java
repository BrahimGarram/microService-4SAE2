package com.freelance.userservice.repository;

import com.freelance.userservice.model.Language;
import com.freelance.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LanguageRepository extends JpaRepository<Language, Long> {
    List<Language> findByUser(User user);
    void deleteByUser(User user);
}
