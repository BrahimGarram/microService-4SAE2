package com.freelance.collaborationservice.service;

import com.freelance.collaborationservice.dto.CompanyDTO;
import com.freelance.collaborationservice.dto.CreateCompanyRequest;
import com.freelance.collaborationservice.exception.ResourceNotFoundException;
import com.freelance.collaborationservice.model.Company;
import com.freelance.collaborationservice.repository.CompanyRepository;
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
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyDTO create(CreateCompanyRequest request) {
        Company company = new Company();
        company.setName(request.getName());
        company.setOwnerId(request.getOwnerId());
        Company saved = companyRepository.save(company);
        log.info("Company created - ID: {}, Name: {}, Owner: {}", saved.getId(), saved.getName(), saved.getOwnerId());
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public CompanyDTO getById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        return toDTO(company);
    }

    @Transactional(readOnly = true)
    public List<CompanyDTO> getByOwnerId(Long ownerId) {
        return companyRepository.findByOwnerId(ownerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Long companyId, Long userId) {
        return companyRepository.existsByIdAndOwnerId(companyId, userId);
    }

    private CompanyDTO toDTO(Company c) {
        return CompanyDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .ownerId(c.getOwnerId())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
