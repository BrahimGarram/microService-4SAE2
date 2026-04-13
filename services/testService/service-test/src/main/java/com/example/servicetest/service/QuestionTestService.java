package com.example.servicetest.service;

import com.example.servicetest.entity.Domain;
import com.example.servicetest.entity.QuestionTest;
import com.example.servicetest.repository.QuestionTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionTestService {

    @Autowired
    private QuestionTestRepository questionTestRepository;


    public QuestionTest addQuestion(QuestionTest question) {
        return questionTestRepository.save(question);
    }


    public List<QuestionTest> getAllQuestions() {
        return questionTestRepository.findAll();
    }


    public QuestionTest getQuestionById(Long id) {
        return questionTestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + id));
    }


    public QuestionTest updateQuestion(Long id, QuestionTest updatedQuestion) {
        QuestionTest existing = getQuestionById(id);

        existing.setQuestionText(updatedQuestion.getQuestionText());
        existing.setDomain(updatedQuestion.getDomain());
        existing.setQuestionType(updatedQuestion.getQuestionType());
        existing.setDifficultyLevel(updatedQuestion.getDifficultyLevel());
        existing.setDuration(updatedQuestion.getDuration());
        existing.setChoices(updatedQuestion.getChoices());
        existing.setCorrectAnswer(updatedQuestion.getCorrectAnswer());
        existing.setIsActive(updatedQuestion.getIsActive());
        if (updatedQuestion.getLanguage() != null) existing.setLanguage(updatedQuestion.getLanguage());
        if (updatedQuestion.getStarterCode() != null) existing.setStarterCode(updatedQuestion.getStarterCode());
        if (updatedQuestion.getTestCasesJson() != null) existing.setTestCasesJson(updatedQuestion.getTestCasesJson());

        return questionTestRepository.save(existing);
    }


    public void deleteQuestion(Long id) {
        questionTestRepository.deleteById(id);
    }



    public List<QuestionTest> getBalancedQuestionsByDomains(List<Domain> domains, int maxQuestions) {
        List<QuestionTest> result = new ArrayList<>();
        int numDomains = domains.size();
        if (numDomains == 0) return result;

        int questionsPerDomain = maxQuestions / numDomains;
        int remainder = maxQuestions % numDomains;

        List<QuestionTest> allActive = questionTestRepository.findAll().stream()
                .filter(q -> q.getIsActive() != null && q.getIsActive())
                .collect(Collectors.toList());

        for (Domain domain : domains) {
            List<QuestionTest> questionsForDomain = allActive.stream()
                    .filter(q -> q.getDomain() == domain)
                    .collect(Collectors.toList());
            Collections.shuffle(questionsForDomain);
            int take = questionsPerDomain;
            if (remainder > 0) {
                take += 1;
                remainder--;
            }
            result.addAll(questionsForDomain.stream().limit(take).collect(Collectors.toList()));
        }

        // Inclure au moins une question CODING si aucune dans le tirage (comparaison par id pour éviter doublon)
        List<QuestionTest> codingPool = questionTestRepository.findActiveCodingQuestions().stream()
                .filter(q -> q.getTestCasesJson() != null && !q.getTestCasesJson().trim().isEmpty())
                .collect(Collectors.toList());
        boolean hasCoding = result.stream().anyMatch(q -> q.getQuestionType() == com.example.servicetest.entity.QuestionType.CODING);
        if (!hasCoding && !codingPool.isEmpty() && result.size() >= 1) {
            Collections.shuffle(codingPool);
            QuestionTest oneCoding = codingPool.get(0);
            boolean alreadyInResult = result.stream().anyMatch(q -> q.getId() != null && q.getId().equals(oneCoding.getId()));
            if (!alreadyInResult) {
                result.remove(result.size() - 1);
                result.add(oneCoding);
            }
        }

        Collections.shuffle(result);
        return result;
    }








}
