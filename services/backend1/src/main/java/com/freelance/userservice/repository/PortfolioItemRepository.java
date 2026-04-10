package com.freelance.userservice.repository;

import com.freelance.userservice.model.PortfolioItem;
import com.freelance.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {
    List<PortfolioItem> findByUserOrderByDisplayOrderAsc(User user);
    void deleteByUser(User user);
}
