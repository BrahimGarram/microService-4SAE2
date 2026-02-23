package com.freelance.collaborationservice.repository;

import com.freelance.collaborationservice.model.CollaborationRequest;
import com.freelance.collaborationservice.model.CollaborationRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollaborationRequestRepository extends JpaRepository<CollaborationRequest, Long> {

    List<CollaborationRequest> findByCollaborationId(Long collaborationId);

    List<CollaborationRequest> findByFreelancerId(Long freelancerId);

    List<CollaborationRequest> findByCollaborationIdAndStatus(Long collaborationId, CollaborationRequestStatus status);

    Optional<CollaborationRequest> findByCollaborationIdAndFreelancerId(Long collaborationId, Long freelancerId);

    boolean existsByCollaborationIdAndFreelancerId(Long collaborationId, Long freelancerId);
}
