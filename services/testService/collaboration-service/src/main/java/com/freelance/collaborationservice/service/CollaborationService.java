package com.freelance.collaborationservice.service;

import com.freelance.collaborationservice.dto.CollaborationDTO;
import com.freelance.collaborationservice.dto.CreateCollaborationRequest;
import com.freelance.collaborationservice.dto.brahim.QuestionTest;
import com.freelance.collaborationservice.dto.UpdateCollaborationRequest;
import com.freelance.collaborationservice.exception.ResourceNotFoundException;
import com.freelance.collaborationservice.interfaces.TestClient;
import com.freelance.collaborationservice.model.Collaboration;
import com.freelance.collaborationservice.model.CollaborationStatus;
import com.freelance.collaborationservice.repository.CollaborationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CollaborationService {
    @Autowired
    TestClient testClient;
    private final CollaborationRepository collaborationRepository;
    private final CompanyService companyService;

    public CollaborationDTO create(CreateCollaborationRequest request) {
        if (request.getBudgetMax().compareTo(request.getBudgetMin()) < 0) {
            throw new IllegalArgumentException("budget_max must be >= budget_min");
        }
        if (request.getConfidentialityOption() == null) {
            request.setConfidentialityOption(false);
        }
        Collaboration collaboration = new Collaboration();
        collaboration.setCompanyId(request.getCompanyId());
        collaboration.setTitle(request.getTitle());
        collaboration.setDescription(request.getDescription());
        collaboration.setCollaborationType(request.getCollaborationType());
        collaboration.setRequiredSkills(request.getRequiredSkills());
        collaboration.setBudgetMin(request.getBudgetMin());
        collaboration.setBudgetMax(request.getBudgetMax());
        collaboration.setEstimatedDuration(request.getEstimatedDuration());
        collaboration.setComplexityLevel(request.getComplexityLevel());
        collaboration.setDeadline(request.getDeadline());
        collaboration.setConfidentialityOption(request.getConfidentialityOption());
        collaboration.setMaxFreelancersNeeded(request.getMaxFreelancersNeeded());
        collaboration.setMilestoneStructure(request.getMilestoneStructure());
        collaboration.setAttachments(request.getAttachments());
        collaboration.setIndustry(request.getIndustry());
        collaboration.setStatus(CollaborationStatus.OPEN);

        Collaboration saved = collaborationRepository.save(collaboration);
        log.info("Collaboration created - ID: {}, Title: {}, Company: {}, Status: OPEN", saved.getId(), saved.getTitle(), saved.getCompanyId());
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public CollaborationDTO getById(Long id) {
        Collaboration c = collaborationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration not found with id: " + id));
        return toDTO(c);
    }

    @Transactional(readOnly = true)
    public List<CollaborationDTO> getAll() {
        return collaborationRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CollaborationDTO> getByCompanyId(Long companyId) {
        return collaborationRepository.findByCompanyId(companyId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CollaborationDTO> getByCompanyIdAndStatus(Long companyId, CollaborationStatus status) {
        return collaborationRepository.findByCompanyIdAndStatus(companyId, status).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CollaborationDTO> getOpen() {
        return collaborationRepository.findByStatus(CollaborationStatus.OPEN).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CollaborationDTO> getByStatus(CollaborationStatus status) {
        return collaborationRepository.findByStatus(status).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CollaborationDTO> getOpenWithFilters(String skills, BigDecimal budgetMin, BigDecimal budgetMax, String estimatedDuration, String industry) {
        List<Collaboration> open = collaborationRepository.findByStatus(CollaborationStatus.OPEN);
        return open.stream()
                .filter(c -> skills == null || skills.isBlank() || (c.getRequiredSkills() != null && c.getRequiredSkills().toLowerCase().contains(skills.toLowerCase())))
                .filter(c -> budgetMin == null || c.getBudgetMin() == null || c.getBudgetMin().compareTo(budgetMin) >= 0)
                .filter(c -> budgetMax == null || c.getBudgetMax() == null || c.getBudgetMax().compareTo(budgetMax) <= 0)
                .filter(c -> estimatedDuration == null || estimatedDuration.isBlank() || (c.getEstimatedDuration() != null && c.getEstimatedDuration().toLowerCase().contains(estimatedDuration.toLowerCase())))
                .filter(c -> industry == null || industry.isBlank() || (c.getIndustry() != null && c.getIndustry().toLowerCase().contains(industry.toLowerCase())))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CollaborationDTO update(Long id, Long companyIdForAuth, boolean adminOverride, UpdateCollaborationRequest request) {
        Collaboration c = collaborationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration not found with id: " + id));
        if (!adminOverride && !collaborationRepository.existsByIdAndCompanyId(id, companyIdForAuth)) {
            throw new IllegalArgumentException("Only the company owner can update this collaboration");
        }
        if (!adminOverride && c.getStatus() != CollaborationStatus.OPEN && c.getStatus() != CollaborationStatus.ON_HOLD) {
            if (request.getBudgetMin() != null || request.getBudgetMax() != null || request.getRequiredSkills() != null) {
                throw new IllegalArgumentException("Budget and required skills are locked after contract is signed");
            }
        }
        if (request.getBudgetMin() != null) c.setBudgetMin(request.getBudgetMin());
        if (request.getBudgetMax() != null) c.setBudgetMax(request.getBudgetMax());
        if (request.getBudgetMin() != null && request.getBudgetMax() != null && request.getBudgetMax().compareTo(request.getBudgetMin()) < 0) {
            throw new IllegalArgumentException("budget_max must be >= budget_min");
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) c.setTitle(request.getTitle());
        if (request.getDescription() != null) c.setDescription(request.getDescription());
        if (request.getCollaborationType() != null) c.setCollaborationType(request.getCollaborationType());
        if (request.getRequiredSkills() != null) c.setRequiredSkills(request.getRequiredSkills());
        if (request.getEstimatedDuration() != null) c.setEstimatedDuration(request.getEstimatedDuration());
        if (request.getComplexityLevel() != null) c.setComplexityLevel(request.getComplexityLevel());
        if (request.getDeadline() != null) c.setDeadline(request.getDeadline());
        if (request.getConfidentialityOption() != null) c.setConfidentialityOption(request.getConfidentialityOption());
        if (request.getMaxFreelancersNeeded() != null) c.setMaxFreelancersNeeded(request.getMaxFreelancersNeeded());
        if (request.getMilestoneStructure() != null) c.setMilestoneStructure(request.getMilestoneStructure());
        if (request.getAttachments() != null) c.setAttachments(request.getAttachments());
        if (request.getIndustry() != null) c.setIndustry(request.getIndustry());
        if (request.getStatus() != null) c.setStatus(request.getStatus());

        Collaboration updated = collaborationRepository.save(c);
        log.info("Collaboration updated - ID: {}", updated.getId());
        return toDTO(updated);
    }

    public void delete(Long id, Long companyIdForAuth, boolean adminOverride) {
        Collaboration c = collaborationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration not found with id: " + id));
        if (!adminOverride && !collaborationRepository.existsByIdAndCompanyId(id, companyIdForAuth)) {
            throw new IllegalArgumentException("Only the company owner can delete this collaboration");
        }
        if (!adminOverride && c.getStatus() != CollaborationStatus.OPEN) {
            throw new IllegalArgumentException("Only OPEN collaborations can be deleted. Use cancel for others.");
        }
        collaborationRepository.deleteById(id);
        log.info("Collaboration deleted - ID: {} (adminOverride={})", id, adminOverride);
    }

    public CollaborationDTO updateStatus(Long id, CollaborationStatus status, Long companyIdForAuth, boolean adminOverride) {
        Collaboration c = collaborationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaboration not found with id: " + id));
        if (!adminOverride && !collaborationRepository.existsByIdAndCompanyId(id, companyIdForAuth)) {
            throw new IllegalArgumentException("Only the company owner can change status");
        }
        c.setStatus(status);
        Collaboration updated = collaborationRepository.save(c);
        return toDTO(updated);
    }

    void setStatusToMatched(Long collaborationId) {
        collaborationRepository.findById(collaborationId).ifPresent(c -> {
            c.setStatus(CollaborationStatus.MATCHED);
            collaborationRepository.save(c);
        });
    }

    private CollaborationDTO toDTO(Collaboration c) {
        return CollaborationDTO.builder()
                .id(c.getId())
                .companyId(c.getCompanyId())
                .title(c.getTitle())
                .description(c.getDescription())
                .collaborationType(c.getCollaborationType())
                .requiredSkills(c.getRequiredSkills())
                .budgetMin(c.getBudgetMin())
                .budgetMax(c.getBudgetMax())
                .estimatedDuration(c.getEstimatedDuration())
                .complexityLevel(c.getComplexityLevel())
                .deadline(c.getDeadline())
                .confidentialityOption(c.getConfidentialityOption())
                .maxFreelancersNeeded(c.getMaxFreelancersNeeded())
                .milestoneStructure(c.getMilestoneStructure())
                .attachments(c.getAttachments())
                .industry(c.getIndustry())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    public List<QuestionTest> getquestions() {
        return testClient.getAllQuestions();
    }
}
