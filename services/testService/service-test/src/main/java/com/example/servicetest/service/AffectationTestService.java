package com.example.servicetest.service;

import com.example.servicetest.entity.AffectationTest;
import com.example.servicetest.entity.Domain;
import com.example.servicetest.entity.QuestionTest;
import com.example.servicetest.entity.QuestionType;
import com.example.servicetest.entity.Status;
import com.example.servicetest.entity.DifficultyLevel;
import com.example.servicetest.repository.AffectationTestRepository;
import com.example.servicetest.repository.QuestionTestRepository;
import com.example.servicetest.api.SubmitTestRequest;
import com.example.servicetest.api.AnswerDto;
import com.example.servicetest.api.RunCodeResponse;
import com.example.servicetest.api.AffectationWithQuestionsDto;
import com.example.servicetest.api.AffectationWithFreelancerDto;
import com.example.servicetest.api.UserSummaryDto;
import com.example.servicetest.client.UserClient;
import com.example.servicetest.api.QuestionForAffectationDto;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AffectationTestService {

    private static final Logger log = LoggerFactory.getLogger(AffectationTestService.class);

    private static class DomainStats {
        double totalPoints = 0;
        int questionCount = 0;
    }

    private final AffectationTestRepository affectationTestRepository;
    private final QuestionTestRepository questionTestRepository;
    private final QuestionTestService questionTestService;
    private final CodeExecutionService codeExecutionService;
    private final EmailService emailService;
    private final UserClient userClient;

    public AffectationTestService(AffectationTestRepository affectationTestRepository,
                                  QuestionTestRepository questionTestRepository,
                                  QuestionTestService questionTestService,
                                  CodeExecutionService codeExecutionService,
                                  EmailService emailService,
                                  UserClient userClient) {
        this.affectationTestRepository = affectationTestRepository;
        this.questionTestRepository = questionTestRepository;
        this.questionTestService = questionTestService;
        this.codeExecutionService = codeExecutionService;
        this.emailService = emailService;
        this.userClient = userClient;
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

        // Domains from freelancer's profile skills (user-service). If none, require to fill profile.
        List<Domain> domains = getDomainsFromUserSkills(freelancerId);
        if (domains == null || domains.isEmpty()) {
            throw new RuntimeException("Please fill your skills in your profile before starting the test.");
        }

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
        affectation.setStatus(Status.IN_PROGRESS);
        for (QuestionTest q : questions) {
            affectation.addQuestion(q);
        }
        return affectationTestRepository.save(affectation);
    }

    /**
     * Récupère les domaines du test à partir des compétences (skills) du profil utilisateur.
     * Appelle le user-service via OpenFeign. Seules les skill.name qui correspondent à l'enum Domain sont retenues.
     * @return liste des domaines (vide si pas de skills ou aucune correspondance)
     */
    private List<Domain> getDomainsFromUserSkills(Long freelancerId) {
        List<Domain> result = new ArrayList<>();
        try {
            UserSummaryDto user = userClient.getUserById(freelancerId);
            if (user == null || user.getSkills() == null || user.getSkills().isEmpty()) {
                return result;
            }
            for (com.example.servicetest.api.SkillSummaryDto skill : user.getSkills()) {
                if (skill == null || skill.getName() == null || skill.getName().isBlank()) {
                    continue;
                }
                String name = skill.getName().trim().toUpperCase();
                try {
                    Domain d = Domain.valueOf(name);
                    if (!result.contains(d)) {
                        result.add(d);
                    }
                } catch (IllegalArgumentException ignored) {
                    // skill name does not match Domain enum, skip
                }
            }
        } catch (Exception e) {
            log.warn("Could not load user skills for freelancer {}: {}", freelancerId, e.getMessage());
        }
        return result;
    }

    // ✅ READ ALL (raw entities, internal use)
    public List<AffectationTest> getAllAffectations() {
        return affectationTestRepository.findAll();
    }

    /**
     * Liste toutes les affectations enrichies avec le nom du freelancer
     * (appel OpenFeign au user-service pour chaque freelancerId).
     */
    public List<AffectationWithFreelancerDto> getAllAffectationsWithFreelancer() {
        List<AffectationTest> entities = affectationTestRepository.findAll();
        return entities.stream()
                .map(this::toAffectationWithFreelancerDto)
                .collect(Collectors.toList());
    }

    private AffectationWithFreelancerDto toAffectationWithFreelancerDto(AffectationTest a) {
        AffectationWithFreelancerDto dto = new AffectationWithFreelancerDto();
        dto.setId(a.getId());
        dto.setFreelancerId(a.getFreelancerId());
        dto.setAssignedAt(a.getAssignedAt());
        dto.setTestDate(a.getTestDate());
        dto.setScore(a.getScore());
        dto.setIsValidated(a.getIsValidated());
        dto.setCorrectAnswersCount(a.getCorrectAnswersCount());
        dto.setTotalQuestions(a.getTotalQuestions());
        dto.setTimeSpent(a.getTimeSpent());
        dto.setStatus(a.getStatus() != null ? a.getStatus().name() : null);
        dto.setDomainScoresJson(a.getDomainScoresJson());

        Long freelancerId = a.getFreelancerId();
        if (freelancerId != null) {
            try {
                UserSummaryDto user = userClient.getUserById(freelancerId);
                if (user != null && user.getUsername() != null) {
                    // Pour l'affichage, on place le username dans freelancerFirstName
                    dto.setFreelancerFirstName(user.getUsername());
                    dto.setFreelancerLastName("");
                }
            } catch (Exception e) {
                log.warn("Unable to fetch freelancer {} from user-service: {}", freelancerId, e.getMessage());
            }
        }
        return dto;
    }

    // ✅ READ BY ID
    public AffectationTest getAffectationById(Long id) {
        return affectationTestRepository.findByIdWithQuestions(id)
                .or(() -> affectationTestRepository.findById(id))
                .orElseThrow(() -> new RuntimeException("Affectation not found with id: " + id));
    }

    /** Même chose que getAffectationById mais en DTO avec questionType en string pour le front (affichage CODING garanti). */
    public AffectationWithQuestionsDto getAffectationWithQuestionsForFront(Long id) {
        AffectationTest a = getAffectationById(id);
        AffectationWithQuestionsDto dto = new AffectationWithQuestionsDto();
        dto.setId(a.getId());
        dto.setFreelancerId(a.getFreelancerId());
        dto.setAssignedAt(a.getAssignedAt());
        dto.setTestDate(a.getTestDate());
        dto.setScore(a.getScore());
        dto.setIsValidated(a.getIsValidated());
        dto.setCorrectAnswersCount(a.getCorrectAnswersCount());
        dto.setTotalQuestions(a.getTotalQuestions());
        dto.setTimeSpent(a.getTimeSpent());
        dto.setStatus(a.getStatus() != null ? a.getStatus().name() : null);
        dto.setDomainScoresJson(a.getDomainScoresJson());
        if (a.getQuestions() != null) {
            dto.setQuestions(a.getQuestions().stream()
                    .map(this::toQuestionForAffectationDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private QuestionForAffectationDto toQuestionForAffectationDto(QuestionTest q) {
        QuestionForAffectationDto dto = new QuestionForAffectationDto();
        dto.setId(q.getId());
        dto.setQuestionText(q.getQuestionText());
        dto.setQuestionType(q.getQuestionType() != null ? q.getQuestionType().name() : "");
        dto.setChoices(q.getChoices() != null ? q.getChoices() : java.util.Collections.emptyList());
        dto.setCorrectAnswer(q.getCorrectAnswer());
        dto.setDuration(q.getDuration());
        dto.setLanguage(q.getLanguage());
        dto.setStarterCode(q.getStarterCode());
        return dto;
    }
    public AffectationTest getAffectationByIdFreelancer(Long id) {
        return affectationTestRepository.findByFreelancerId(id)
                .orElseThrow(() -> new RuntimeException("Affectation not found with id: " + id));
    }


    // ✅ UPDATE (soumission du test → statut COMPLETED automatiquement)
    public AffectationTest updateAffectation(Long id, AffectationTest updated) {

        AffectationTest existing = getAffectationById(id);

        existing.setFreelancerId(updated.getFreelancerId());
        existing.setTestDate(updated.getTestDate());
        existing.setScore(updated.getScore());
        existing.setIsValidated(updated.getIsValidated());
        existing.setCorrectAnswersCount(updated.getCorrectAnswersCount());
        existing.setTotalQuestions(updated.getTotalQuestions());
        existing.setTimeSpent(updated.getTimeSpent());
        // Quand le front envoie score + testDate (soumission), on passe en COMPLETED
        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        } else if (updated.getTestDate() != null && updated.getScore() != null) {
            existing.setStatus(Status.COMPLETED);
        }

        return affectationTestRepository.save(existing);
    }

    /** Marque l'affectation comme abandonnée (freelancer a quitté sans soumettre). */
    @Transactional
    public AffectationTest markAbandoned(Long id) {
        AffectationTest existing = getAffectationById(id);
        if (existing.getStatus() == Status.IN_PROGRESS) {
            existing.setStatus(Status.ABANDONED);
            return affectationTestRepository.save(existing);
        }
        return existing;
    }

    /** Marque l'affectation comme fraude (changement d'onglet, aucun visage, ou visage ne correspond pas). */
    @Transactional
    public AffectationTest markFraud(Long id) {
        AffectationTest existing = getAffectationById(id);
        if (existing.getStatus() == Status.IN_PROGRESS) {
            existing.setStatus(Status.FRAUD);
            AffectationTest saved = affectationTestRepository.saveAndFlush(existing);
            log.info("Affectation id={} marquée comme FRAUD.", id);
            return saved;
        }
        log.debug("Affectation id={} non marquée FRAUD (statut actuel: {}).", id, existing.getStatus());
        return existing;
    }

    /**
     * Soumission du test avec réponses : le backend calcule le score (QCM/VRAI_FAUX par comparaison, CODING par exécution).
     */
    @Transactional
    public AffectationTest submitTest(Long affectationId, SubmitTestRequest request) {
        AffectationTest affectation = getAffectationById(affectationId);
        if (affectation.getStatus() != Status.IN_PROGRESS) {
            return affectation;
        }
        java.util.Set<QuestionTest> questions = affectation.getQuestions();
        if (questions == null || questions.isEmpty()) {
            return affectation;
        }
        java.util.Map<Long, String> answerByQuestionId = new java.util.HashMap<>();
        if (request.getAnswers() != null) {
            for (AnswerDto a : request.getAnswers()) {
                if (a.getQuestionId() != null) {
                    answerByQuestionId.put(a.getQuestionId(), a.getAnswer() != null ? a.getAnswer() : "");
                }
            }
        }
        int totalQuestions = questions.size();
        double totalPoints = 0;
        int correctCount = 0;
        // Stats par domaine (pour calculer un score par domaine)
        java.util.Map<Domain, DomainStats> domainStats = new java.util.HashMap<>();
        for (QuestionTest q : questions) {
            Long qid = q.getId();
            String answer = answerByQuestionId.get(qid);
            Domain domain = q.getDomain();
            DomainStats stats = null;
            if (domain != null) {
                stats = domainStats.computeIfAbsent(domain, d -> new DomainStats());
            }
            double pointsForQuestion = 0;
            boolean isCorrectForStats = false;
            if (q.getQuestionType() == QuestionType.CODING) {
                int codeScore = 0;
                if (answer != null && !answer.isBlank()) {
                    try {
                        RunCodeResponse run = codeExecutionService.runCode(answer, qid);
                        codeScore = run != null ? run.getScore() : 0;
                    } catch (Exception ex) {
                        System.err.println("[AffectationTestService] Erreur exécution code (question " + qid + "): " + ex.getMessage());
                        codeScore = 0;
                    }
                }
                totalPoints += codeScore;
                pointsForQuestion = codeScore;
                if (codeScore >= 60) {
                    correctCount++;
                    isCorrectForStats = true;
                }
            } else {
                boolean correct = isAnswerCorrectForQuestion(answer, q.getCorrectAnswer());
                double pts = correct ? 100 : 0;
                totalPoints += pts;
                pointsForQuestion = pts;
                if (correct) {
                    correctCount++;
                    isCorrectForStats = true;
                }
            }
            if (stats != null) {
                stats.totalPoints += pointsForQuestion;
                stats.questionCount++;
            }

            // Mise à jour des statistiques de qualité sur la question elle-même
            int attempts = q.getAttemptsCount() != null ? q.getAttemptsCount() : 0;
            attempts++;
            q.setAttemptsCount(attempts);

            int success = q.getSuccessCount() != null ? q.getSuccessCount() : 0;
            if (isCorrectForStats) {
                success++;
            }
            q.setSuccessCount(success);

            // Temps moyen approximatif par question (temps total / nb de questions)
            double timePerQuestion = totalQuestions > 0 ? (double) request.getTimeSpent() / totalQuestions : 0.0;
            double oldAvg = q.getAvgTimeSeconds() != null ? q.getAvgTimeSeconds() : 0.0;
            double newAvg = attempts == 1 ? timePerQuestion : ((oldAvg * (attempts - 1)) + timePerQuestion) / attempts;
            q.setAvgTimeSeconds(newAvg);

            // Ajustement automatique de la difficulté en fonction du taux de réussite
            if (attempts >= 5) { // on attend au moins 5 tentatives pour éviter le bruit
                double successRate = success > 0 ? (double) success / attempts * 100.0 : 0.0;
                if (successRate >= 90.0) {
                    q.setDifficultyLevel(DifficultyLevel.EASY);
                } else if (successRate <= 20.0) {
                    q.setDifficultyLevel(DifficultyLevel.HARD);
                } else if (successRate >= 40.0 && successRate <= 80.0) {
                    q.setDifficultyLevel(DifficultyLevel.MEDIUM);
                }
            }

            // Persiste la question mise à jour
            questionTestRepository.save(q);
        }
        int score = totalQuestions > 0 ? (int) Math.round(totalPoints / totalQuestions) : 0;
        affectation.setScore((double) score);
        affectation.setCorrectAnswersCount(correctCount);
        affectation.setTotalQuestions(totalQuestions);
        affectation.setTimeSpent(request.getTimeSpent());
        affectation.setTestDate(java.time.LocalDateTime.now());
        affectation.setIsValidated(score >= 60);
        affectation.setStatus(Status.COMPLETED);

        // Calcul et stockage du score par domaine (JSON)
        java.util.Map<String, Integer> domainScores = new java.util.HashMap<>();
        for (java.util.Map.Entry<Domain, DomainStats> entry : domainStats.entrySet()) {
            Domain d = entry.getKey();
            DomainStats ds = entry.getValue();
            if (ds.questionCount > 0) {
                int pct = (int) Math.round(ds.totalPoints / ds.questionCount);
                domainScores.put(d.name(), pct);
            }
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = mapper.writeValueAsString(domainScores);
            affectation.setDomainScoresJson(json);
        } catch (Exception e) {
            System.err.println("[AffectationTestService] Impossible de sérialiser les scores par domaine : " + e.getMessage());
        }
        if (request.getAnswers() != null) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                affectation.setAnswersJson(mapper.writeValueAsString(request.getAnswers()));
            } catch (Exception e) {
                System.err.println("[AffectationTestService] Impossible de sérialiser les réponses : " + e.getMessage());
            }
        }

        AffectationTest saved;
        try {
            saved = affectationTestRepository.save(affectation);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("answers_json") || msg.contains("Unknown column") || (e.getCause() != null && e.getCause().getMessage() != null && e.getCause().getMessage().contains("answers_json"))) {
                affectation.setAnswersJson(null);
                saved = affectationTestRepository.save(affectation);
            } else {
                throw e;
            }
        }

        // Envoi d'un email de récapitulatif au freelancer connecté (si email fourni).
        // On protège cet appel pour qu'aucune erreur SMTP ne casse la soumission du test.
        try {
            emailService.sendTestResultEmail(
                    request.getFreelancerEmail(),
                    request.getFreelancerName(),
                    saved
            );
        } catch (Exception e) {
            System.err.println("[AffectationTestService] Erreur lors de l'envoi de l'e-mail de résultat : " + e.getMessage());
            e.printStackTrace();
        }

        return saved;
    }

    private static boolean isAnswerCorrectForQuestion(String userAnswer, String correctAnswer) {
        if (userAnswer == null) userAnswer = "";
        if (correctAnswer == null) correctAnswer = "";
        String u = userAnswer.trim().toLowerCase().replaceAll("\\s+", " ");
        String c = correctAnswer.trim().toLowerCase().replaceAll("\\s+", " ");
        if (u.equals("true")) u = "vrai";
        if (u.equals("false")) u = "faux";
        if (c.equals("true")) c = "vrai";
        if (c.equals("false")) c = "faux";
        return u.equals(c);
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
