package com.example.servicetest.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.example.servicetest.api.Views;
import com.example.servicetest.validation.ValidQuestionTest;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@ValidQuestionTest
public class QuestionTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Question text is required (10 to 1000 characters).")
    @Size(min = 10, max = 1000, message = "Question text must be between 10 and 1000 characters.")
    @Column(nullable = false, length = 1000)
    private String questionText;

    @NotNull(message = "Domain is required.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Domain domain;

    @NotNull(message = "Question type is required.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType;

    @NotNull(message = "Difficulty level is required.")
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Min(value = 1, message = "Duration must be at least 1 second.")
    private Integer duration;

    @ElementCollection
    @CollectionTable(name = "question_choices", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "choice")
    private List<String> choices = new ArrayList<>();

    @Lob
    private String correctAnswer;

    private Boolean isActive = true;

    /** Questions CODING : langage (java, python, javascript) */
    @Column(length = 50)
    private String language;

    /** Questions CODING : code de départ dans l'éditeur */
    @Lob
    private String starterCode;

    /** Questions CODING : cas de test JSON - jamais envoyé au candidat (vue Public). LONGTEXT pour éviter troncature (TINYTEXT = 255 octets). */
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String testCasesJson;

    private LocalDateTime createdAt = LocalDateTime.now();

    /** Statistiques de qualité : nombre total de tentatives où cette question est apparue. */
    private Integer attemptsCount = 0;

    /** Statistiques de qualité : nombre de fois où la question a été réussie. */
    private Integer successCount = 0;

    /** Statistiques de qualité : temps moyen passé (en secondes, approximatif). */
    private Double avgTimeSeconds = 0.0;

    // 🔥 On ignore pour éviter la boucle infinie
    @JsonIgnore
    @ManyToMany(mappedBy = "questions")
    private Set<AffectationTest> affectations = new HashSet<>();

    public QuestionTest() {}

    // Getters & Setters

    public Long getId() { return id; }

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
    public void setIsActive(Boolean active) { isActive = active; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getStarterCode() { return starterCode; }
    public void setStarterCode(String starterCode) { this.starterCode = starterCode; }

    @JsonView(Views.Admin.class)
    public String getTestCasesJson() { return testCasesJson; }
    public void setTestCasesJson(String testCasesJson) { this.testCasesJson = testCasesJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Set<AffectationTest> getAffectations() { return affectations; }

    public Integer getAttemptsCount() {
        return attemptsCount;
    }

    public void setAttemptsCount(Integer attemptsCount) {
        this.attemptsCount = attemptsCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Double getAvgTimeSeconds() {
        return avgTimeSeconds;
    }

    public void setAvgTimeSeconds(Double avgTimeSeconds) {
        this.avgTimeSeconds = avgTimeSeconds;
    }
}
