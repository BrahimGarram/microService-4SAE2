package com.freelance.collaborationservice.repository;

import com.freelance.collaborationservice.model.Collaboration;
import com.freelance.collaborationservice.model.CollaborationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollaborationRepository extends JpaRepository<Collaboration, Long> {

    List<Collaboration> findByCompanyId(Long companyId);

    List<Collaboration> findByCompanyIdAndStatus(Long companyId, CollaborationStatus status);

    List<Collaboration> findByStatus(CollaborationStatus status);

    boolean existsByIdAndCompanyId(Long id, Long companyId);
}
