package com.example.servicetest.controller;

import com.example.servicetest.Dto.ProjectDTO;
import com.example.servicetest.entity.Domain;
import com.example.servicetest.entity.QuestionTest;
import com.example.servicetest.service.QuestionTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/questions")
public class QuestionTestController {

    @Autowired
    private QuestionTestService questionTestService;

    // CREATE
    @PostMapping
    public QuestionTest createQuestion(@RequestBody QuestionTest question) {
        return questionTestService.addQuestion(question);
    }

    // READ ALL
    @GetMapping
    public List<QuestionTest> getAllQuestions() {
        return questionTestService.getAllQuestions();
    }

    // READ BY ID
    @GetMapping("/{id}")
    public QuestionTest getQuestionById(@PathVariable Long id) {
        return questionTestService.getQuestionById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public QuestionTest updateQuestion(@PathVariable Long id,
                                       @RequestBody QuestionTest question) {
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
@RequestMapping("/sahm")
    public ResponseEntity<List<ProjectDTO>> getallProjects(){
        return questionTestService.getallProjects();
    }
}
