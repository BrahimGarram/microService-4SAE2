package com.example.servicetest.controller;

import com.example.servicetest.entity.Domain;
import com.example.servicetest.entity.QuestionTest;
import com.example.servicetest.service.QuestionTestService;
import com.example.servicetest.api.Views;
import com.example.servicetest.api.AiQuestionGenerateRequest;
import com.example.servicetest.api.AiQuestionGenerateResponse;
import com.example.servicetest.service.AiQuestionAgentClient;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/questions")
public class QuestionTestController {

    @Autowired
    private QuestionTestService questionTestService;

    @Autowired
    private AiQuestionAgentClient aiQuestionAgentClient;

    // CREATE
    @PostMapping
    public QuestionTest createQuestion(@Valid @RequestBody QuestionTest question) {
        return questionTestService.addQuestion(question);
    }

    // READ ALL
    @GetMapping
    public List<QuestionTest> getAllQuestions() {
        return questionTestService.getAllQuestions();
    }

    // READ BY ID (admin : renvoie tous les champs de la question)
    @GetMapping("/{id}")
    public QuestionTest getQuestionById(@PathVariable Long id) {
        return questionTestService.getQuestionById(id);
    }

    /**
     * Endpoint admin : demande à l'agent IA (microservice Python) de générer des questions.
     * L'agent est STRICTEMENT dédié à la génération de questions.
     */
    @PostMapping("/ai/generate")
    public ResponseEntity<AiQuestionGenerateResponse> generateQuestionsWithAi(
            @RequestBody AiQuestionGenerateRequest request
    ) {
        AiQuestionGenerateResponse response = aiQuestionAgentClient.generateQuestions(request);
        return ResponseEntity.ok(response);
    }

    // UPDATE
    @PutMapping("/{id}")
    public QuestionTest updateQuestion(@PathVariable Long id,
                                       @Valid @RequestBody QuestionTest question) {
        return questionTestService.updateQuestion(id, question);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deleteQuestion(@PathVariable Long id) {
        questionTestService.deleteQuestion(id);
    }

    @GetMapping("/getbydomains")
    public List<QuestionTest> getQuestionsByDomain() {

        List<Domain> domains = new ArrayList<>();
        domains.add(Domain.JAVA);
        domains.add(Domain.ANGULAR);
      return questionTestService.getBalancedQuestionsByDomains(domains,8);

    }


}
