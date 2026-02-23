package com.freelance.collaborationservice.service;

import com.freelance.collaborationservice.dto.CollaborationRequestDTO;
import com.freelance.collaborationservice.dto.CreateCollaborationRequestDTO;
import com.freelance.collaborationservice.dto.UpdateCollaborationRequestStatusDTO;
import com.freelance.collaborationservice.exception.ResourceNotFoundException;
import com.freelance.collaborationservice.model.Collaboration;
import com.freelance.collaborationservice.model.CollaborationRequest;
import com.freelance.collaborationservice.model.CollaborationRequestStatus;
import com.freelance.collaborationservice.model.CollaborationStatus;
import com.freelance.collaborationservice.repository.CollaborationRepository;
import com.freelance.collaborationservice.repository.CollaborationRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CollaborationRequestService {

    private final CollaborationRequestRepository requestRepository;
    private final CollaborationRepository collaborationRepository;
    private final CollaborationService collaborationService;

    public CollaborationRequestDTO create(CreateCollaborationRequestDTO dto) {
        if (!collaborationRepository.existsById(dto.getCollaborationId())) {
            throw new ResourceNotFoundException("Collaboration not found with id: " + dto.getCollaborationId());
        }
        CollaborationStatus status = collaborationRepository.findById(dto.getCollaborationId())
                .map(Collaboration::getStatus).orElse(null);
        if (status != CollaborationStatus.OPEN) {
            throw new IllegalArgumentException("Cannot apply to a collaboration that is not OPEN");
        }
        if (requestRepository.existsByCollaborationIdAndFreelancerId(dto.getCollaborationId(), dto.getFreelancerId())) {
            throw new IllegalArgumentException("You have already applied to this collaboration");
        }
        CollaborationRequest req = new CollaborationRequest();
        req.setCollaborationId(dto.getCollaborationId());
        req.setFreelancerId(dto.getFreelancerId());
        req.setProposalMessage(dto.getProposalMessage());
        req.setProposedPrice(dto.getProposedPrice());
        req.setStatus(CollaborationRequestStatus.PENDING);
        CollaborationRequest saved = requestRepository.save(req);
        log.info("Collaboration request created - ID: {}, Collaboration: {}, Freelancer: {}", saved.getId(), saved.getCollaborationId(), saved.getFreelancerId());
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public CollaborationRequestDTO getById(Long id) {
        CollaborationRequest req = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration request not found with id: " + id));
        return toDTO(req);
    }

    @Transactional(readOnly = true)
    public List<CollaborationRequestDTO> getByCollaborationId(Long collaborationId) {
        return requestRepository.findByCollaborationId(collaborationId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CollaborationRequestDTO> getByFreelancerId(Long freelancerId) {
        return requestRepository.findByFreelancerId(freelancerId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Get all collaboration requests (admin use). Use when no collaborationId or freelancerId filter is provided.
     */
    @Transactional(readOnly = true)
    public List<CollaborationRequestDTO> getAll() {
        return requestRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public CollaborationRequestDTO updateStatus(Long id, UpdateCollaborationRequestStatusDTO dto, Long companyIdForAuth) {
        CollaborationRequest req = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration request not found with id: " + id));
        Collaboration collab = collaborationRepository.findById(req.getCollaborationId())
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration not found"));
        if (!collaborationRepository.existsByIdAndCompanyId(collab.getId(), companyIdForAuth)) {
            throw new IllegalArgumentException("Only the company owner can accept/reject applications");
        }
        if (req.getStatus() != CollaborationRequestStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING requests can be updated");
        }
        req.setStatus(dto.getStatus());
        CollaborationRequest saved = requestRepository.save(req);
        if (dto.getStatus() == CollaborationRequestStatus.ACCEPTED) {
            collaborationService.setStatusToMatched(req.getCollaborationId());
        }
        log.info("Collaboration request {} - ID: {}, Status: {}", dto.getStatus(), id, dto.getStatus());
        return toDTO(saved);
    }

    public void withdraw(Long id, Long freelancerId) {
        CollaborationRequest req = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration request not found with id: " + id));
        if (!req.getFreelancerId().equals(freelancerId)) {
            throw new IllegalArgumentException("Only the applicant can withdraw");
        }
        if (req.getStatus() != CollaborationRequestStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING requests can be withdrawn");
        }
        requestRepository.delete(req);
        log.info("Collaboration request withdrawn - ID: {}", id);
    }

    public CollaborationRequestDTO updateProposal(Long id, String proposalMessage, java.math.BigDecimal proposedPrice, Long freelancerId) {
        CollaborationRequest req = requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration request not found with id: " + id));
        if (!req.getFreelancerId().equals(freelancerId)) {
            throw new IllegalArgumentException("Only the applicant can edit the proposal");
        }
        if (req.getStatus() != CollaborationRequestStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING requests can be edited");
        }
        if (proposalMessage != null) req.setProposalMessage(proposalMessage);
        if (proposedPrice != null) req.setProposedPrice(proposedPrice);
        return toDTO(requestRepository.save(req));
    }

    private CollaborationRequestDTO toDTO(CollaborationRequest r) {
        return CollaborationRequestDTO.builder()
                .id(r.getId())
                .collaborationId(r.getCollaborationId())
                .freelancerId(r.getFreelancerId())
                .proposalMessage(r.getProposalMessage())
                .proposedPrice(r.getProposedPrice())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
