package com.example.servicetest.service;

import com.example.servicetest.Dto.ProjectDTO;
import com.example.servicetest.entity.Domain;
import com.example.servicetest.entity.QuestionTest;
import com.example.servicetest.interfaces.ProjectClient;
import com.example.servicetest.repository.QuestionTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuestionTestService {
    @Autowired
    ProjectClient projectClient;
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

        return questionTestRepository.save(existing);
    }


    public void deleteQuestion(Long id) {
        questionTestRepository.deleteById(id);
    }



    public List<QuestionTest> getBalancedQuestionsByDomains(List<Domain> domains, int maxQuestions) {
        List<QuestionTest> result = new ArrayList<>();
        int numDomains = domains.size();
        if (numDomains == 0) return result;

        // Nombre de questions par domaine (répartitions équitables)
        int questionsPerDomain = maxQuestions / numDomains;
        int remainder = maxQuestions % numDomains; // Pour répartir les restes

        for (Domain domain : domains) {
            // Récupérer toutes les questions pour ce domaine
            List<QuestionTest> questionsForDomain = questionTestRepository.findAll()
                    .stream()
                    .filter(q -> q.getDomain() == domain)
                    .collect(Collectors.toList());

            // Mélanger aléatoirement
            Collections.shuffle(questionsForDomain);

            // Calcul du nombre à prendre pour ce domaine
            int take = questionsPerDomain;
            if (remainder > 0) { // ajouter 1 question aux premiers domaines si reste
                take += 1;
                remainder--;
            }

            // Ajouter au résultat (ou moins si pas assez de questions disponibles)
            result.addAll(questionsForDomain.stream()
                    .limit(take)
                    .collect(Collectors.toList()));
        }

        // Mélanger le résultat final pour que l'ordre soit aléatoire
        Collections.shuffle(result);

        return result;
    }


    public ResponseEntity<List<ProjectDTO>> getallProjects() {
        return projectClient.getAllProjects();

    }






}
