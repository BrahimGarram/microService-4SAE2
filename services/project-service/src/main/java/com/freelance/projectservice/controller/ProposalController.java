package com.freelance.projectservice.controller;

import com.freelance.projectservice.dto.CreateProposalRequest;
import com.freelance.projectservice.dto.ProposalDTO;
import com.freelance.projectservice.service.ProposalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for proposals (bids) on projects.
 */
@RestController
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProposalController {

    private final ProposalService proposalService;

    @PostMapping
    public ResponseEntity<ProposalDTO> create(@Valid @RequestBody CreateProposalRequest request) {
        ProposalDTO created = proposalService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ProposalDTO>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(proposalService.findByProjectId(projectId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProposalDTO>> getMyProposals(@RequestParam Long freelancerId) {
        return ResponseEntity.ok(proposalService.findByFreelancerId(freelancerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProposalDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(proposalService.getById(id));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ProposalDTO> accept(@PathVariable Long id, @RequestParam Long ownerId) {
        return ResponseEntity.ok(proposalService.accept(id, ownerId));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ProposalDTO> reject(@PathVariable Long id, @RequestParam Long ownerId) {
        return ResponseEntity.ok(proposalService.reject(id, ownerId));
    }
}
