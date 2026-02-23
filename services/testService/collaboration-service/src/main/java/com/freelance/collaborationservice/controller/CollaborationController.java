package com.freelance.collaborationservice.controller;

import com.freelance.collaborationservice.dto.*;
import com.freelance.collaborationservice.dto.brahim.QuestionTest;
import com.freelance.collaborationservice.model.CollaborationStatus;
import com.freelance.collaborationservice.service.CollaborationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/collaborations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class CollaborationController {



    private final CollaborationService collaborationService;

    @PostMapping
    public ResponseEntity<CollaborationDTO> create(@Valid @RequestBody CreateCollaborationRequest request) {
        CollaborationDTO created = collaborationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollaborationDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(collaborationService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<CollaborationDTO>> getAll(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) CollaborationStatus status,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) BigDecimal budgetMin,
            @RequestParam(required = false) BigDecimal budgetMax,
            @RequestParam(required = false) String estimatedDuration,
            @RequestParam(required = false) String industry) {
        if (companyId != null && status != null) {
            return ResponseEntity.ok(collaborationService.getByCompanyIdAndStatus(companyId, status));
        }
        if (companyId != null) {
            return ResponseEntity.ok(collaborationService.getByCompanyId(companyId));
        }
        if (status != null) {
            if (status == CollaborationStatus.OPEN && (skills != null || budgetMin != null || budgetMax != null || estimatedDuration != null || industry != null)) {
                return ResponseEntity.ok(collaborationService.getOpenWithFilters(skills, budgetMin, budgetMax, estimatedDuration, industry));
            }
            if (status == CollaborationStatus.OPEN) {
                return ResponseEntity.ok(collaborationService.getOpen());
            }
            return ResponseEntity.ok(collaborationService.getByStatus(status));
        }
        // No companyId, no status: return ALL collaborations (e.g. admin dashboard)
        List<CollaborationDTO> all = collaborationService.getAll();
        log.info("GET /api/collaborations (no params): returning {} collaborations", all.size());
        return ResponseEntity.ok(all);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollaborationDTO> update(
            @PathVariable Long id,
            @RequestParam Long companyId,
            @RequestParam(required = false, defaultValue = "false") boolean adminOverride,
            @Valid @RequestBody UpdateCollaborationRequest request) {
        return ResponseEntity.ok(collaborationService.update(id, companyId, adminOverride, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CollaborationDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam CollaborationStatus status,
            @RequestParam Long companyId,
            @RequestParam(required = false, defaultValue = "false") boolean adminOverride) {
        return ResponseEntity.ok(collaborationService.updateStatus(id, status, companyId, adminOverride));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam Long companyId,
            @RequestParam(required = false, defaultValue = "false") boolean adminOverride) {
        collaborationService.delete(id, companyId, adminOverride);
        return ResponseEntity.noContent().build();
    }







//  Partie micro service de brahim
    @RequestMapping("/brahim")
    public List<QuestionTest> questionTests() {
        return collaborationService.getquestions();
    }








}
