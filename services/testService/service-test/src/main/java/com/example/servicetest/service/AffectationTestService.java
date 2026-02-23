package com.example.servicetest.service;

import com.example.servicetest.Dto.ProjectDTO;
import com.example.servicetest.entity.AffectationTest;
import com.example.servicetest.entity.Domain;
import com.example.servicetest.entity.QuestionTest;
import com.example.servicetest.entity.Status;
import com.example.servicetest.interfaces.ProjectClient;
import com.example.servicetest.repository.AffectationTestRepository;
import com.example.servicetest.repository.QuestionTestRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AffectationTestService {



    private final AffectationTestRepository affectationTestRepository;
    private final QuestionTestRepository questionTestRepository;
    private final QuestionTestService questionTestService;

    public AffectationTestService(AffectationTestRepository affectationTestRepository,
                                  QuestionTestRepository questionTestRepository,
                                  QuestionTestService questionTestService) {
        this.affectationTestRepository = affectationTestRepository;
        this.questionTestRepository = questionTestRepository;
        this.questionTestService = questionTestService;
    }

    // ✅ CREATE
    @Transactional
    public AffectationTest createAffectation(AffectationTest affectation) {

        Long freelancerId = affectation.getFreelancerId();
        List<AffectationTest> existing = affectationTestRepository.findAllByFreelancerIdOrderByAssignedAtDesc(freelancerId);

        // Si déjà validé une fois -> ne peut plus repasser le test
        boolean hasValidated = existing.stream().anyMatch(a -> Boolean.TRUE.equals(a.getIsValidated()));
        if (hasValidated) {
            throw new RuntimeException("Vous avez déjà réussi le test. Vous ne pouvez pas le repasser.");
        }

        // Max 3 échecs (tentatives non validées)
        long failureCount = existing.stream().filter(a -> !Boolean.TRUE.equals(a.getIsValidated())).count();
        if (failureCount >= 3) {
            throw new RuntimeException("Vous avez atteint le maximum de 3 tentatives. Vous ne pouvez plus repasser le test.");
        }

        return affectationTestRepository.save(affectation);
    }

    /** Liste toutes les affectations d'un freelancer (pour le front) */
    public List<AffectationTest> getAffectationsByFreelancerId(Long freelancerId) {
        return affectationTestRepository.findAllByFreelancerIdOrderByAssignedAtDesc(freelancerId);
    }

    /**
     * Démarrer un test pour un freelancer : vérifie les règles (1 succès ou 3 échecs max),
     * tire des questions par domaines, crée l'affectation et la retourne.
     */
    @Transactional
    public AffectationTest startTest(Long freelancerId) {
        List<AffectationTest> existing = affectationTestRepository.findAllByFreelancerIdOrderByAssignedAtDesc(freelancerId);
        boolean hasValidated = existing.stream().anyMatch(a -> Boolean.TRUE.equals(a.getIsValidated()));
        if (hasValidated) {
            throw new RuntimeException("Vous avez déjà réussi le test. Vous ne pouvez pas le repasser.");
        }
        long failureCount = existing.stream().filter(a -> !Boolean.TRUE.equals(a.getIsValidated())).count();
        if (failureCount >= 3) {
            throw new RuntimeException("Vous avez atteint le maximum de 3 tentatives. Vous ne pouvez plus repasser le test.");
        }

        List<Domain> domains = java.util.Arrays.asList(Domain.JAVA, Domain.ANGULAR);
        List<QuestionTest> questions = questionTestService.getBalancedQuestionsByDomains(domains, 8);
        if (questions.isEmpty()) {
            throw new RuntimeException("Aucune question disponible pour le test.");
        }

        AffectationTest affectation = new AffectationTest();
        affectation.setFreelancerId(freelancerId);
        affectation.setTotalQuestions(questions.size());
        affectation.setCorrectAnswersCount(0);
        affectation.setScore(0.0);
        affectation.setIsValidated(false);
        affectation.setStatus(Status.ACTIVE);
        for (QuestionTest q : questions) {
            affectation.addQuestion(q);
        }
        return affectationTestRepository.save(affectation);
    }
    // ✅ READ ALL
    public List<AffectationTest> getAllAffectations() {
        return affectationTestRepository.findAll();
    }

    // ✅ READ BY ID
    public AffectationTest getAffectationById(Long id) {
        return affectationTestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Affectation not found with id: " + id));
    }
    public AffectationTest getAffectationByIdFreelancer(Long id) {
        return affectationTestRepository.findByFreelancerId(id)
                .orElseThrow(() -> new RuntimeException("Affectation not found with id: " + id));
    }


    // ✅ UPDATE
    public AffectationTest updateAffectation(Long id, AffectationTest updated) {

        AffectationTest existing = getAffectationById(id);

        existing.setFreelancerId(updated.getFreelancerId());
        existing.setTestDate(updated.getTestDate());
        existing.setScore(updated.getScore());
        existing.setIsValidated(updated.getIsValidated());
        existing.setCorrectAnswersCount(updated.getCorrectAnswersCount());
        existing.setTotalQuestions(updated.getTotalQuestions());
        existing.setTimeSpent(updated.getTimeSpent());
        existing.setStatus(updated.getStatus());

        return affectationTestRepository.save(existing);
    }

    // ✅ DELETE
    public void deleteAffectation(Long  id) {
        AffectationTest affectation = getAffectationById(id);
        affectationTestRepository.delete(affectation);
    }

    // ✅ Ajouter une question à une affectation
    public AffectationTest addQuestionToAffectation(Long affectationId, Long questionId) {

        AffectationTest affectation = getAffectationById(affectationId);

        QuestionTest question = questionTestRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        affectation.addQuestion(question);

        return affectationTestRepository.save(affectation);
    }

    // ✅ Supprimer une question d’une affectation
    public AffectationTest removeQuestionFromAffectation(Long affectationId, Long questionId) {

        AffectationTest affectation = getAffectationById(affectationId);

        QuestionTest question = questionTestRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        affectation.removeQuestion(question);

        return affectationTestRepository.save(affectation);
    }
    public long countScoreGreaterOrEqual50() {
        return affectationTestRepository.findAll()
                .stream()
                .filter(a -> a.getScore() != null && a.getScore() >= 50)
                .count();
    }

    // 🎯 Scores < 50%
    public long countScoreLessThan50() {
        return affectationTestRepository.findAll()
                .stream()
                .filter(a -> a.getScore() != null && a.getScore() < 50)
                .count();
    }

    // 🎯 Validated = true
    public long countValidatedTrue() {
        return affectationTestRepository.findAll()
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsValidated()))
                .count();
    }

    // 🎯 Validated = false
    public long countValidatedFalse() {
        return affectationTestRepository.findAll()
                .stream()
                .filter(a -> Boolean.FALSE.equals(a.getIsValidated()))
                .count();
    }

    // 🎯 Freelancer validé dès la première tentative (1 seule ligne + validated=true)
    public long countFreelancerValidatedFirstTime() {
        return affectationTestRepository.findAll()
                .stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsValidated()))
                .map(AffectationTest::getFreelancerId)
                .distinct()
                .filter(freelancerId ->
                        affectationTestRepository.countByFreelancerId(freelancerId) == 1
                )
                .count();
    }

    // 🎯 Statistique par Status
    public long countByStatus(Status status) {
        return affectationTestRepository.findAll()
                .stream()
                .filter(a -> a.getStatus() == status)
                .count();
    }



    }











