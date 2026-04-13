package com.freelance.userservice.controller;

import com.freelance.userservice.dto.ChatRequest;
import com.freelance.userservice.dto.ChatResponse;
import com.freelance.userservice.service.FreelancifyChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Freelancify AI chat (Ollama). Authenticated users only.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final FreelancifyChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request);
        return ResponseEntity.ok(response);
    }
}
