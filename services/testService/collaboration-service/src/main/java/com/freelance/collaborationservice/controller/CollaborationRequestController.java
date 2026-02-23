package com.freelance.collaborationservice.controller;

import com.freelance.collaborationservice.dto.CollaborationRequestDTO;
import com.freelance.collaborationservice.dto.CreateCollaborationRequestDTO;
import com.freelance.collaborationservice.dto.UpdateCollaborationRequestStatusDTO;
import com.freelance.collaborationservice.dto.UpdateProposalDTO;
import com.freelance.collaborationservice.service.CollaborationRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collaboration-requests")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CollaborationRequestController {

    private final CollaborationRequestService collaborationRequestService;

    @PostMapping
    public ResponseEntity<CollaborationRequestDTO> create(@Valid @RequestBody CreateCollaborationRequestDTO dto) {
        CollaborationRequestDTO created = collaborationRequestService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollaborationRequestDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(collaborationRequestService.getById(id));
    }

    @GetMapping("/collaboration/{collaborationId}")
    public ResponseEntity<List<CollaborationRequestDTO>> getByCollaborationId(@PathVariable Long collaborationId) {
        return ResponseEntity.ok(collaborationRequestService.getByCollaborationId(collaborationId));
    }

    @GetMapping("/freelancer/{freelancerId}")
    public ResponseEntity<List<CollaborationRequestDTO>> getByFreelancerId(@PathVariable Long freelancerId) {
        return ResponseEntity.ok(collaborationRequestService.getByFreelancerId(freelancerId));
    }

    /**
     * Get all collaboration requests (admin). Call with no params to list every application.
     */
    @GetMapping
    public ResponseEntity<List<CollaborationRequestDTO>> getAll(
            @RequestParam(required = false) Long collaborationId,
            @RequestParam(required = false) Long freelancerId) {
        if (collaborationId != null) {
            return ResponseEntity.ok(collaborationRequestService.getByCollaborationId(collaborationId));
        }
        if (freelancerId != null) {
            return ResponseEntity.ok(collaborationRequestService.getByFreelancerId(freelancerId));
        }
        return ResponseEntity.ok(collaborationRequestService.getAll());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CollaborationRequestDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam Long companyId,
            @Valid @RequestBody UpdateCollaborationRequestStatusDTO dto) {
        return ResponseEntity.ok(collaborationRequestService.updateStatus(id, dto, companyId));
    }

    @DeleteMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(@PathVariable Long id, @RequestParam Long freelancerId) {
        collaborationRequestService.withdraw(id, freelancerId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CollaborationRequestDTO> updateProposal(
            @PathVariable Long id,
            @RequestParam Long freelancerId,
            @Valid @RequestBody UpdateProposalDTO body) {
        return ResponseEntity.ok(collaborationRequestService.updateProposal(id, body.getProposalMessage(), body.getProposedPrice(), freelancerId));
    }
}
