package com.example.servicetest.api;

import java.util.List;

/**
 * Réponse renvoyée à l'admin : inclut le message de l'agent IA et la liste
 * de propositions de questions.
 */
public class AiQuestionGenerateResponse {

    private boolean success;
    private String message;
    private boolean needsNumberOfQuestions;
    private List<AiGeneratedQuestionDto> questions;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isNeedsNumberOfQuestions() {
        return needsNumberOfQuestions;
    }

    public void setNeedsNumberOfQuestions(boolean needsNumberOfQuestions) {
        this.needsNumberOfQuestions = needsNumberOfQuestions;
    }

    public List<AiGeneratedQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<AiGeneratedQuestionDto> questions) {
        this.questions = questions;
    }
}
