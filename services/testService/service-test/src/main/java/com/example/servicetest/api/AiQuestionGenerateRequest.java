package com.example.servicetest.api;

import com.example.servicetest.entity.Domain;

import java.util.List;

/**
 * Requête envoyée depuis l'admin (/admin/test) vers le backend pour demander à l'agent IA
 * de générer des questions.
 */
public class AiQuestionGenerateRequest {

    private String prompt;
    private Integer numberOfQuestions;
    private List<Domain> domains;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public List<Domain> getDomains() {
        return domains;
    }

    public void setDomains(List<Domain> domains) {
        this.domains = domains;
    }
}
