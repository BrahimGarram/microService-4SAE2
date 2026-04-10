package com.example.servicetest.api;

import java.util.List;

/**
 * Soumission du test : réponses par question. Le backend calcule le score (exécution CODING côté serveur).
 */
public class SubmitTestRequest {
    private int timeSpent;
    private List<AnswerDto> answers;
    /** Email du freelancer connecté (destinataire du récapitulatif de test). */
    private String freelancerEmail;
    /** Nom affiché dans l'email (username ou nom complet). */
    private String freelancerName;

    public int getTimeSpent() { return timeSpent; }
    public void setTimeSpent(int timeSpent) { this.timeSpent = timeSpent; }
    public List<AnswerDto> getAnswers() { return answers; }
    public void setAnswers(List<AnswerDto> answers) { this.answers = answers; }

    public String getFreelancerEmail() {
        return freelancerEmail;
    }

    public void setFreelancerEmail(String freelancerEmail) {
        this.freelancerEmail = freelancerEmail;
    }

    public String getFreelancerName() {
        return freelancerName;
    }

    public void setFreelancerName(String freelancerName) {
        this.freelancerName = freelancerName;
    }
}
