package com.example.servicetest.service;

import com.example.servicetest.api.RunCodeResponse;
import com.example.servicetest.entity.QuestionTest;
import com.example.servicetest.entity.QuestionType;
import org.springframework.stereotype.Service;

/**
 * Service d'exécution de code : charge la question et délègue au DockerCodeRunner.
 */
@Service
public class CodeExecutionService {

    private final QuestionTestService questionTestService;
    private final DockerCodeRunner dockerCodeRunner;

    public CodeExecutionService(QuestionTestService questionTestService, DockerCodeRunner dockerCodeRunner) {
        this.questionTestService = questionTestService;
        this.dockerCodeRunner = dockerCodeRunner;
    }

    /**
     * Exécute le code pour une question CODING et retourne le score (sans exposer les sorties attendues).
     */
    public RunCodeResponse runCode(String code, Long questionId) {
        QuestionTest question = questionTestService.getQuestionById(questionId);
        if (question.getQuestionType() != QuestionType.CODING) {
            RunCodeResponse r = new RunCodeResponse();
            r.setSuccess(false);
            r.setMessage("This question is not of type CODING.");
            r.setScore(0);
            r.setTotalCount(0);
            r.setPassedCount(0);
            return r;
        }
        String testCasesJson = question.getTestCasesJson();
        if (testCasesJson == null || testCasesJson.isBlank()) {
            RunCodeResponse r = new RunCodeResponse();
            r.setSuccess(false);
            r.setMessage("No test cases configured.");
            r.setScore(0);
            r.setTotalCount(0);
            r.setPassedCount(0);
            return r;
        }
        return dockerCodeRunner.run(code, question.getLanguage(), testCasesJson);
    }
}
