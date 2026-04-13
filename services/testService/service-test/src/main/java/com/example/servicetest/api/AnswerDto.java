package com.example.servicetest.api;

/**
 * Une réponse : soit le choix (QCM/VRAI_FAUX), soit le code (CODING).
 */
public class AnswerDto {
    private Long questionId;
    private String answer;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}
