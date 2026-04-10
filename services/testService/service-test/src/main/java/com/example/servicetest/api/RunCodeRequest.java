package com.example.servicetest.api;

/**
 * Requête pour exécuter le code du candidat (bouton Exécuter ou évaluation à la soumission).
 */
public class RunCodeRequest {
    private String code;
    private Long questionId;

    public RunCodeRequest() {}

    public RunCodeRequest(String code, Long questionId) {
        this.code = code;
        this.questionId = questionId;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
}
