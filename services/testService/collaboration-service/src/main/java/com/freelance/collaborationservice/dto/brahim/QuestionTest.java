package com.freelance.collaborationservice.dto.brahim;

import java.time.LocalDateTime;
import java.util.List;

public class QuestionTest {
    private Long id;
    private String questionText;
    private Domain domain;
    private QuestionType questionType;
    private DifficultyLevel difficultyLevel;
    private Integer duration;
    private List<String> choices;
    private String correctAnswer;
    private Boolean isActive;
    private LocalDateTime createdAt;

    // Constructeurs
    public QuestionTest() {}

    public QuestionTest(Long id, String questionText, Domain domain, QuestionType questionType,
                           DifficultyLevel difficultyLevel, Integer duration, List<String> choices,
                           String correctAnswer, Boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.questionText = questionText;
        this.domain = domain;
        this.questionType = questionType;
        this.difficultyLevel = difficultyLevel;
        this.duration = duration;
        this.choices = choices;
        this.correctAnswer = correctAnswer;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public Domain getDomain() { return domain; }
    public void setDomain(Domain domain) { this.domain = domain; }

    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }

    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }

    public List<String> getChoices() { return choices; }
    public void setChoices(List<String> choices) { this.choices = choices; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
