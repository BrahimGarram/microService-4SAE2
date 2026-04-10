package com.example.servicetest.service;

import com.example.servicetest.entity.AffectationTest;
import com.example.servicetest.entity.QuestionTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiTestFeedbackService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String configuredApiKey;

    @Value("${gemini.model:}")
    private String configuredModel;

    public AiTestFeedbackService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    private String resolveApiKey() {
        String key = configuredApiKey;
        if (key == null || key.isBlank()) {
            key = System.getenv("GEMINI_API_KEY");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY manquant (propriété gemini.api.key ou variable d'environnement GEMINI_API_KEY).");
        }
        return key;
    }

    private String resolveModel() {
        String model = configuredModel;
        if (model == null || model.isBlank()) {
            model = System.getenv("GEMINI_MODEL");
        }
        if (model == null || model.isBlank()) {
            model = "gemini-2.5-flash";
        }
        return model;
    }

    /**
     * Génère un feedback textuel en français sur un test terminé, en utilisant Gemini.
     */
    public String generateFeedbackForAffectation(AffectationTest affectation, String userMessage) {
        if (affectation == null) {
            throw new IllegalArgumentException("Affectation null.");
        }

        String apiKey = resolveApiKey();
        String model = resolveModel();

        double score = affectation.getScore() != null ? affectation.getScore() : 0.0;
        boolean validated = Boolean.TRUE.equals(affectation.getIsValidated());
        int totalQuestions = affectation.getTotalQuestions() != null ? affectation.getTotalQuestions() : 0;
        int correct = affectation.getCorrectAnswersCount() != null ? affectation.getCorrectAnswersCount() : 0;
        int timeSpent = affectation.getTimeSpent() != null ? affectation.getTimeSpent() : 0;

        Map<String, Integer> domainScores = new HashMap<>();
        if (affectation.getDomainScoresJson() != null) {
            try {
                domainScores = objectMapper.readValue(
                        affectation.getDomainScoresJson(),
                        new TypeReference<Map<String, Integer>>() {}
                );
            } catch (Exception ignored) {
            }
        }

        Map<Long, String> userAnswerByQuestionId = new HashMap<>();
        if (affectation.getAnswersJson() != null) {
            try {
                List<Map<String, Object>> answersList = objectMapper.readValue(
                        affectation.getAnswersJson(),
                        new TypeReference<List<Map<String, Object>>>() {}
                );
                for (Map<String, Object> a : answersList) {
                    Object qid = a.get("questionId");
                    Object ans = a.get("answer");
                    if (qid instanceof Number) {
                        userAnswerByQuestionId.put(((Number) qid).longValue(), ans != null ? ans.toString() : "");
                    }
                }
            } catch (Exception ignored) {
            }
        }

        Set<QuestionTest> questionsSet = affectation.getQuestions();
        List<QuestionTest> questionsOrdered = questionsSet == null ? List.of() :
                questionsSet.stream().sorted(Comparator.comparing(QuestionTest::getId)).collect(Collectors.toList());

        StringBuilder fullTestContext = new StringBuilder();
        fullTestContext.append("--- DÉTAIL DU TEST (questions, réponses du candidat, bonnes réponses) ---\n\n");
        int num = 1;
        for (QuestionTest q : questionsOrdered) {
            Long qid = q.getId();
            String userAnswer = userAnswerByQuestionId.getOrDefault(qid, "");
            String correctAnswer = q.getCorrectAnswer() != null ? q.getCorrectAnswer() : "(évaluation serveur pour question CODING)";
            if (userAnswer != null && userAnswer.length() > 2000) {
                userAnswer = userAnswer.substring(0, 2000) + "... [tronqué]";
            }
            fullTestContext.append("Question ").append(num).append(" [")
                    .append(q.getDomain() != null ? q.getDomain().name() : "").append(" / ")
                    .append(q.getQuestionType() != null ? q.getQuestionType().name() : "").append("]\n")
                    .append("Énoncé : ").append(q.getQuestionText() != null ? q.getQuestionText() : "").append("\n")
                    .append("Réponse du candidat : ").append(userAnswer.isEmpty() ? "(vide)" : userAnswer).append("\n")
                    .append("Bonne réponse : ").append(correctAnswer).append("\n\n");
            num++;
        }

        StringBuilder userContext = new StringBuilder();
        userContext.append("Tu es le coach IA dédié au feedback de CE test technique. Tu as accès à TOUT le test : chaque question, la réponse du candidat et la bonne réponse.\n\n")
                .append("Résultats globaux :\n")
                .append("- Score : ").append(score).append("% (").append(correct).append(" / ").append(totalQuestions).append(").\n")
                .append("- Test validé (seuil 60%) : ").append(validated ? "OUI" : "NON").append(".\n")
                .append("- Temps passé : ").append(timeSpent).append(" s.\n")
                .append("- Scores par domaine (%) : ").append(domainScores).append(".\n\n")
                .append(fullTestContext)
                .append("--- FIN DU DÉTAIL DU TEST ---\n\n");

        if (userMessage == null || userMessage.isBlank()) {
            userContext.append("Génère un rapport TRÈS COURT, LISIBLE et bien structuré en français. Règles strictes :\n")
                    .append("- Utilise exactement les 3 titres suivants (avec ###) :\n")
                    .append("### 1) Points forts\n")
                    .append("### 2) Points à améliorer\n")
                    .append("### 3) Recommandations\n\n")
                    .append("- Dans chaque section : uniquement des puces courtes. Maximum 3 à 4 puces par section.\n");
        } else {
            userContext.append("Question du candidat :\n").append(userMessage.trim()).append("\n\n")
                    .append("Réponds de façon COURTE et concrète en t'appuyant sur ce test.\n");
        }

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> part = new HashMap<>();
        part.put("text", userContext.toString());
        content.put("role", "user");
        content.put("parts", List.of(part));
        requestBody.put("contents", List.of(content));

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        Map body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("Réponse vide de Gemini.");
        }

        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new IllegalStateException("Aucun candidat renvoyé par Gemini.");
            }
            Map<String, Object> first = candidates.get(0);
            Map<String, Object> contentMap = (Map<String, Object>) first.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
            if (parts == null || parts.isEmpty()) {
                throw new IllegalStateException("Aucun contenu renvoyé par Gemini.");
            }
            Object text = parts.get(0).get("text");
            if (text == null) {
                throw new IllegalStateException("Texte vide renvoyé par Gemini.");
            }
            return text.toString().trim();
        } catch (Exception e) {
            throw new IllegalStateException("Format inattendu de la réponse Gemini: " + e.getMessage(), e);
        }
    }

    public String generateFeedbackForAffectation(AffectationTest affectation) {
        return generateFeedbackForAffectation(affectation, null);
    }
}
