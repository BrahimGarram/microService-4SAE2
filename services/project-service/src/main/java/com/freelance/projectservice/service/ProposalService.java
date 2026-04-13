package com.freelance.projectservice.service;

import com.freelance.projectservice.dto.CreateProposalRequest;
import com.freelance.projectservice.dto.ProposalDTO;
import com.freelance.projectservice.exception.ResourceNotFoundException;
import com.freelance.projectservice.model.Project;
import com.freelance.projectservice.model.ProjectStatus;
import com.freelance.projectservice.model.Proposal;
import com.freelance.projectservice.model.ProposalStatus;
import com.freelance.projectservice.repository.ProjectRepository;
import com.freelance.projectservice.repository.ProposalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final ProjectRepository projectRepository;

    public ProposalService(ProposalRepository proposalRepository, ProjectRepository projectRepository) {
        this.proposalRepository = proposalRepository;
        this.projectRepository = projectRepository;
    }

    public ProposalDTO create(CreateProposalRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));
        if (project.getStatus() != ProjectStatus.OPEN) {
            throw new IllegalArgumentException("Proposals are only allowed on OPEN projects");
        }
        if (proposalRepository.existsByProjectIdAndFreelancerId(request.getProjectId(), request.getFreelancerId())) {
            throw new IllegalArgumentException("You have already submitted a proposal for this project");
        }
        Proposal proposal = new Proposal();
        proposal.setProjectId(request.getProjectId());
        proposal.setFreelancerId(request.getFreelancerId());
        proposal.setAmount(request.getAmount());
        proposal.setProposedDeadline(request.getProposedDeadline());
        proposal.setMessage(request.getMessage());
        proposal.setStatus(ProposalStatus.PENDING);
        Proposal saved = proposalRepository.save(proposal);
        log.info("Proposal created - id: {}, project: {}, freelancer: {}", saved.getId(), saved.getProjectId(), saved.getFreelancerId());
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ProposalDTO> findByProjectId(Long projectId) {
        return proposalRepository.findByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProposalDTO> findByFreelancerId(Long freelancerId) {
        return proposalRepository.findByFreelancerIdOrderByCreatedAtDesc(freelancerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProposalDTO getById(Long id) {
        Proposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + id));
        return toDTO(proposal);
    }

    public ProposalDTO accept(Long proposalId, Long ownerId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + proposalId));
        Project project = projectRepository.findById(proposal.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!project.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Only the project owner can accept a proposal");
        }
        if (proposal.getStatus() != ProposalStatus.PENDING && proposal.getStatus() != ProposalStatus.NEGOTIATING) {
            throw new IllegalArgumentException("Proposal is no longer open for acceptance");
        }
        proposal.setStatus(ProposalStatus.ACCEPTED);
        Proposal saved = proposalRepository.save(proposal);
        project.setStatus(ProjectStatus.IN_PROGRESS);
        projectRepository.save(project);
        log.info("Proposal accepted - id: {}, project: {}", saved.getId(), saved.getProjectId());
        return toDTO(saved);
    }

    public ProposalDTO reject(Long proposalId, Long ownerId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + proposalId));
        Project project = projectRepository.findById(proposal.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!project.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Only the project owner can reject a proposal");
        }
        if (proposal.getStatus() != ProposalStatus.PENDING && proposal.getStatus() != ProposalStatus.NEGOTIATING) {
            throw new IllegalArgumentException("Proposal is no longer open for rejection");
        }
        proposal.setStatus(ProposalStatus.REJECTED);
        Proposal saved = proposalRepository.save(proposal);
        log.info("Proposal rejected - id: {}", saved.getId());
        return toDTO(saved);
    }

    private ProposalDTO toDTO(Proposal p) {
        return ProposalDTO.builder()
                .id(p.getId())
                .projectId(p.getProjectId())
                .freelancerId(p.getFreelancerId())
                .amount(p.getAmount())
                .proposedDeadline(p.getProposedDeadline())
                .message(p.getMessage())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
