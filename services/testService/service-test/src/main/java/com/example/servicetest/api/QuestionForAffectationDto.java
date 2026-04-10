package com.example.servicetest.api;

import java.util.List;

/**
 * DTO pour une question telle que renvoyée au front lors du chargement d'une affectation.
 * Garantit que questionType est bien une chaîne "CODING", "QCM_SIMPLE", "VRAI_FAUX" pour l'affichage Angular.
 */
public class QuestionForAffectationDto {
    private Long id;
    private String questionText;
    /** Toujours une chaîne : CODING, QCM_SIMPLE, VRAI_FAUX */
    private String questionType;
    private List<String> choices;
    private String correctAnswer;
    private Integer duration;
    private String language;
    private String starterCode;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public List<String> getChoices() { return choices; }
    public void setChoices(List<String> choices) { this.choices = choices; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getStarterCode() { return starterCode; }
    public void setStarterCode(String starterCode) { this.starterCode = starterCode; }
}
