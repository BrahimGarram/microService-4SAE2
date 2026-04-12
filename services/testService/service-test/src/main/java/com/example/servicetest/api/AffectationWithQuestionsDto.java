package com.example.servicetest.api;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour l'affectation renvoyée au front (GET by-id), avec questions en forme garantie (questionType en string).
 */
public class AffectationWithQuestionsDto {
    private Long id;
    private Long freelancerId;
    private LocalDateTime assignedAt;
    private LocalDateTime testDate;
    private Double score;
    private Boolean isValidated;
    private Integer correctAnswersCount;
    private Integer totalQuestions;
    private Integer timeSpent;
    private String status;
    private String domainScoresJson;
    private List<QuestionForAffectationDto> questions;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getTestDate() { return testDate; }
    public void setTestDate(LocalDateTime testDate) { this.testDate = testDate; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Boolean getIsValidated() { return isValidated; }
    public void setIsValidated(Boolean isValidated) { this.isValidated = isValidated; }

    public Integer getCorrectAnswersCount() { return correctAnswersCount; }
    public void setCorrectAnswersCount(Integer correctAnswersCount) { this.correctAnswersCount = correctAnswersCount; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Integer getTimeSpent() { return timeSpent; }
    public void setTimeSpent(Integer timeSpent) { this.timeSpent = timeSpent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDomainScoresJson() {
        return domainScoresJson;
    }

    public void setDomainScoresJson(String domainScoresJson) {
        this.domainScoresJson = domainScoresJson;
    }

    public List<QuestionForAffectationDto> getQuestions() { return questions; }
    public void setQuestions(List<QuestionForAffectationDto> questions) { this.questions = questions; }
}
