package com.example.servicetest.service;

import com.example.servicetest.api.AiQuestionGenerateRequest;
import com.example.servicetest.api.AiQuestionGenerateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * Client HTTP vers le microservice Python (FastAPI) question-agent.
 * L'agent est dédié EXCLUSIVEMENT à la génération de questions.
 */
@Service
public class AiQuestionAgentClient {

    private static final Logger log = LoggerFactory.getLogger(AiQuestionAgentClient.class);

    private final RestTemplate restTemplate;

    @Value("${ai.question-agent.base-url:http://localhost:8001}")
    private String baseUrl;

    public AiQuestionAgentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AiQuestionGenerateResponse generateQuestions(AiQuestionGenerateRequest req) {
        String url = baseUrl + "/ai/questions/generate";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AiQuestionGenerateRequest> entity = new HttpEntity<>(req, headers);
        try {
            ResponseEntity<AiQuestionGenerateResponse> resp = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    AiQuestionGenerateResponse.class
            );
            AiQuestionGenerateResponse body = resp.getBody();
            if (body == null) {
                AiQuestionGenerateResponse fallback = new AiQuestionGenerateResponse();
                fallback.setSuccess(false);
                fallback.setMessage("Réponse vide de l'agent IA.");
                fallback.setNeedsNumberOfQuestions(false);
                fallback.setQuestions(Collections.emptyList());
                return fallback;
            }
            return body;
        } catch (Exception e) {
            log.warn("Appel à l'agent IA question-agent échoué: {}", e.getMessage());
            AiQuestionGenerateResponse fallback = new AiQuestionGenerateResponse();
            fallback.setSuccess(false);
            fallback.setMessage("Impossible de contacter l'agent IA (question-agent). Assurez-vous qu'il est démarré.");
            fallback.setNeedsNumberOfQuestions(false);
            fallback.setQuestions(Collections.emptyList());
            return fallback;
        }
    }
}
