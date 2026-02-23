package com.example.servicetest.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
public class QuestionTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Domain domain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    private Integer duration;

    @ElementCollection
    @CollectionTable(name = "question_choices", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "choice")
    private List<String> choices = new ArrayList<>();

    @Lob
    private String correctAnswer;

    private Boolean isActive = true;

    private LocalDateTime createdAt = LocalDateTime.now();

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

    public LocalDateTime getCreatedAt() { return createdAt; }

    public Set<AffectationTest> getAffectations() { return affectations; }
}
