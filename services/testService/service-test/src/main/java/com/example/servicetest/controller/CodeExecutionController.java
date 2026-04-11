package com.example.servicetest.controller;

import com.example.servicetest.api.RunCodeRequest;
import com.example.servicetest.api.RunCodeResponse;
import com.example.servicetest.service.CodeExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/code")
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;

    public CodeExecutionController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    /**
     * Exécute le code pour une question CODING (bouton Exécuter ou évaluation).
     * Retourne le score et la sortie du candidat, jamais la sortie attendue.
     */
    @PostMapping("/run")
    public ResponseEntity<RunCodeResponse> runCode(@RequestBody RunCodeRequest request) {
        if (request.getQuestionId() == null) {
            return ResponseEntity.badRequest().build();
        }
        RunCodeResponse response = codeExecutionService.runCode(
                request.getCode() != null ? request.getCode() : "",
                request.getQuestionId()
        );
        return ResponseEntity.ok(response);
    }
}
