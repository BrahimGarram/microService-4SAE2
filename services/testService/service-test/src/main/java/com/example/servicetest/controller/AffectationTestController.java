package com.example.servicetest.controller;

import com.example.servicetest.entity.AffectationTest;
import com.example.servicetest.entity.Status;
import com.example.servicetest.service.AffectationTestService;
import com.example.servicetest.service.AiTestFeedbackService;
import com.example.servicetest.api.SubmitTestRequest;
import com.example.servicetest.api.AffectationWithQuestionsDto;
import com.example.servicetest.api.AffectationWithFreelancerDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/affectations")
public class AffectationTestController {

    private final AffectationTestService affectationTestService;
    private final AiTestFeedbackService aiTestFeedbackService;

    public AffectationTestController(AffectationTestService affectationTestService,
                                     AiTestFeedbackService aiTestFeedbackService) {
        this.affectationTestService = affectationTestService;
        this.aiTestFeedbackService = aiTestFeedbackService;
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
    public ResponseEntity<?> startTest(@RequestBody java.util.Map<String, Object> body) {
        Object raw = body.get("freelancerId");
        if (raw == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "freelancerId required"));
        }
        Long freelancerId = null;
        if (raw instanceof Number) {
            freelancerId = ((Number) raw).longValue();
        } else if (raw instanceof String) {
            try {
                freelancerId = Long.parseLong((String) raw);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(java.util.Map.of("message", "Invalid freelancerId"));
            }
        }
        if (freelancerId == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "Invalid freelancerId"));
        }
        try {
            return ResponseEntity.ok(affectationTestService.startTest(freelancerId));
        } catch (RuntimeException e) {
            String message = e.getMessage() != null ? e.getMessage() : "Unable to start the test.";
            return ResponseEntity.status(400).body(java.util.Map.of("message", message));
        }
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

    /** Récupérer une affectation par son id (pour passer le test) — DTO avec questionType en string pour afficher CODING. */
    @GetMapping("/by-id/{id}")
    public ResponseEntity<AffectationWithQuestionsDto> getAffectationById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                affectationTestService.getAffectationWithQuestionsForFront(id)
        );
    }

    /** Liste toutes les affectations (admin dashboard) avec nom du freelancer */
    @GetMapping
    public ResponseEntity<List<AffectationWithFreelancerDto>> getAllAffectations() {
        return ResponseEntity.ok(affectationTestService.getAllAffectationsWithFreelancer());
    }


    // ✅ DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAffectation(
            @PathVariable Long id) {

        affectationTestService.deleteAffectation(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ UPDATE (soumission du test → front envoie status COMPLETED)
    @PutMapping("/{id}")
    public ResponseEntity<AffectationTest> updateAffectation(
            @PathVariable Long id,
            @RequestBody AffectationTest updated) {

        return ResponseEntity.ok(
                affectationTestService.updateAffectation(id, updated)
        );
    }

    /** Marque l'affectation comme abandonnée (freelancer a quitté sans soumettre). */
    @PatchMapping("/{id}/abandon")
    public ResponseEntity<AffectationTest> abandonTest(@PathVariable Long id) {
        return ResponseEntity.ok(affectationTestService.markAbandoned(id));
    }

    /** Marque l'affectation comme fraude (changement d'onglet, pas de visage, ou visage différent de la photo). */
    @PatchMapping("/{id}/fraud")
    public ResponseEntity<AffectationTest> reportFraud(@PathVariable Long id) {
        return ResponseEntity.ok(affectationTestService.markFraud(id));
    }

    /** Soumission du test avec réponses (QCM + CODING) ; le backend calcule le score. */
    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitTest(
            @PathVariable Long id,
            @RequestBody SubmitTestRequest request) {
        try {
            return ResponseEntity.ok(affectationTestService.submitTest(id, request));
        } catch (Exception e) {
            System.err.println("[AffectationTestController] Erreur submit test: " + e.getMessage());
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : "Server error while submitting the test.";
            return ResponseEntity.status(500).body(Map.of("error", msg));
        }
    }

    /** Feedback IA (Gemini) pour un test complété (message optionnel du freelancer). */
    @PostMapping("/{id}/ai/feedback")
    public ResponseEntity<Map<String, String>> getAiFeedback(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body
    ) {
        try {
            AffectationTest affectation = affectationTestService.getAffectationById(id);
            String message = body != null ? body.getOrDefault("message", "") : "";
            String feedback = aiTestFeedbackService.generateFeedbackForAffectation(affectation, message);
            return ResponseEntity.ok(Map.of("feedback", feedback));
        } catch (Exception e) {
            System.err.println("[AffectationTestController] Erreur feedback IA: " + e.getMessage());
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : "";
            String userMsg;
            if (msg.contains("429") || msg.contains("Too Many Requests") || msg.contains("quota") || msg.contains("RESOURCE_EXHAUSTED")) {
                userMsg = "Gemini API quota exceeded (free limit: 20 requests/day). Try again in 1 minute or tomorrow.";
            } else {
                userMsg = "Error generating AI feedback: " + (msg.length() > 80 ? msg.substring(0, 80) + "…" : msg);
            }
            return ResponseEntity.status(500).body(
                    Map.of("feedback", "", "error", userMsg)
            );
        }
    }
 //                            Partie statistique
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
