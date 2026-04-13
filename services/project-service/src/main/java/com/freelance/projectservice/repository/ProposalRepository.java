package com.freelance.projectservice.repository;

import com.freelance.projectservice.model.Proposal;
import com.freelance.projectservice.model.ProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    List<Proposal> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    List<Proposal> findByFreelancerIdOrderByCreatedAtDesc(Long freelancerId);

    List<Proposal> findByProjectIdAndStatus(Long projectId, ProposalStatus status);

    boolean existsByProjectIdAndFreelancerId(Long projectId, Long freelancerId);
}
