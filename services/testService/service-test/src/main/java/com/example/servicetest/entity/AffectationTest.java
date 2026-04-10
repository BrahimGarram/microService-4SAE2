package com.example.servicetest.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class AffectationTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long freelancerId;

    private LocalDateTime assignedAt;

    private LocalDateTime testDate;

    private Double score;

    private Boolean isValidated;

    private Integer correctAnswersCount;

    private Integer totalQuestions;

    private Integer timeSpent;

    @Lob
    private String domainScoresJson;

    /** Réponses du candidat (JSON: [{"questionId":1,"answer":"..."}, ...]) pour le feedback IA. */
    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = true)
    private String answersJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "affectation_question",
            joinColumns = @JoinColumn(name = "affectation_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<QuestionTest> questions = new HashSet<>();

    public AffectationTest() {
        this.assignedAt = LocalDateTime.now();
        this.status = Status.IN_PROGRESS;
        this.isValidated = false;
        this.score = 0.0;
    }

    // Helper methods (IMPORTANT)
    public void addQuestion(QuestionTest question) {
        this.questions.add(question);
        question.getAffectations().add(this);
    }

    public void removeQuestion(QuestionTest question) {
        this.questions.remove(question);
        question.getAffectations().remove(this);
    }

    // Getters & Setters

    public Long getId() { return id; }

    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }

    public LocalDateTime getAssignedAt() { return assignedAt; }

    public LocalDateTime getTestDate() { return testDate; }
    public void setTestDate(LocalDateTime testDate) { this.testDate = testDate; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Boolean getIsValidated() { return isValidated; }
    public void setIsValidated(Boolean validated) { isValidated = validated; }

    public Integer getCorrectAnswersCount() { return correctAnswersCount; }
    public void setCorrectAnswersCount(Integer correctAnswersCount) { this.correctAnswersCount = correctAnswersCount; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Integer getTimeSpent() { return timeSpent; }
    public void setTimeSpent(Integer timeSpent) { this.timeSpent = timeSpent; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Set<QuestionTest> getQuestions() { return questions; }

    public String getDomainScoresJson() {
        return domainScoresJson;
    }

    public void setDomainScoresJson(String domainScoresJson) {
        this.domainScoresJson = domainScoresJson;
    }

    public String getAnswersJson() {
        return answersJson;
    }

    public void setAnswersJson(String answersJson) {
        this.answersJson = answersJson;
    }
}
