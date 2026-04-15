package com.freelance.userservice.repository;

import com.freelance.userservice.model.SocialLink;
import com.freelance.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {
    List<SocialLink> findByUser(User user);
    void deleteByUser(User user);
}
