package com.example.servicetest.controller;

import com.example.servicetest.entity.AffectationTest;
import com.example.servicetest.entity.Status;
import com.example.servicetest.service.AffectationTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/affectations")
public class AffectationTestController {

    private final AffectationTestService affectationTestService;

    public AffectationTestController(AffectationTestService affectationTestService) {
        this.affectationTestService = affectationTestService;
    }

    // ✅ CREATE
    @PostMapping
    public ResponseEntity<AffectationTest> createAffectation(
            @RequestBody AffectationTest affectation) {

        return ResponseEntity.ok(
                affectationTestService.createAffectation(affectation)
        );
    }

    /** Démarrer un test : crée une affectation avec questions pour le freelancer */
    @PostMapping("/start")
    public ResponseEntity<AffectationTest> startTest(@RequestBody java.util.Map<String, Long> body) {
        Long freelancerId = body.get("freelancerId");
        if (freelancerId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(
                affectationTestService.startTest(freelancerId)
        );
    }





    /** Une seule affectation par freelancer (dernière) - pour compat */
    @GetMapping("/{idfreelancer}")
    public ResponseEntity<AffectationTest> getAffectationByFreelancerId(
            @PathVariable("idfreelancer") Long freelancerId) {
        return ResponseEntity.ok(
                affectationTestService.getAffectationByIdFreelancer(freelancerId)
        );
    }

    /** Liste toutes les affectations d'un freelancer (pour vérifier droit de passer le test) */
    @GetMapping("/by-freelancer/{freelancerId}")
    public ResponseEntity<List<AffectationTest>> getAffectationsByFreelancerId(
            @PathVariable Long freelancerId) {
        return ResponseEntity.ok(
                affectationTestService.getAffectationsByFreelancerId(freelancerId)
        );
    }

    /** Récupérer une affectation par son id (pour passer le test) */
    @GetMapping("/by-id/{id}")
    public ResponseEntity<AffectationTest> getAffectationById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                affectationTestService.getAffectationById(id)
        );
    }

    /** Liste toutes les affectations (admin dashboard) */
    @GetMapping
    public ResponseEntity<List<AffectationTest>> getAllAffectations() {
        return ResponseEntity.ok(affectationTestService.getAllAffectations());
    }


    // ✅ DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAffectation(
            @PathVariable Long id) {

        affectationTestService.deleteAffectation(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<AffectationTest> updateAffectation(
            @PathVariable Long id,
            @RequestBody AffectationTest updated) {

        return ResponseEntity.ok(
                affectationTestService.updateAffectation(id, updated)
        );
    }
    @GetMapping("/stats/score/success")
    public ResponseEntity<Long> countScoreSuccess() {
        return ResponseEntity.ok(
                affectationTestService.countScoreGreaterOrEqual50()
        );
    }

    // 🎯 Score < 50%
    @GetMapping("/stats/score/fail")
    public ResponseEntity<Long> countScoreFail() {
        return ResponseEntity.ok(
                affectationTestService.countScoreLessThan50()
        );
    }

    // 🎯 Validated true
    @GetMapping("/stats/validated/true")
    public ResponseEntity<Long> countValidatedTrue() {
        return ResponseEntity.ok(
                affectationTestService.countValidatedTrue()
        );
    }

    // 🎯 Validated false
    @GetMapping("/stats/validated/false")
    public ResponseEntity<Long> countValidatedFalse() {
        return ResponseEntity.ok(
                affectationTestService.countValidatedFalse()
        );
    }

    // 🎯 Freelancer validé dès la première tentative
    @GetMapping("/stats/validated-first-time")
    public ResponseEntity<Long> countValidatedFirstTime() {
        return ResponseEntity.ok(
                affectationTestService.countFreelancerValidatedFirstTime()
        );
    }

    // 🎯 Statistique par Status
    @GetMapping("/stats/status/{status}")
    public ResponseEntity<Long> countByStatus(@PathVariable Status status) {
        return ResponseEntity.ok(
                affectationTestService.countByStatus(status)
        );
    }


}
