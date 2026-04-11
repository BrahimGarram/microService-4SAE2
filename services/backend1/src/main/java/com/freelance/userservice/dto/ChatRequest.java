package com.freelance.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * Request for Freelancify AI chat (Ollama).
 */
@Data
public class ChatRequest {

    /** User's message */
    @NotBlank(message = "Message cannot be empty")
    private String message;

    /** Optional conversation history for context (last N messages). */
    private List<ChatMessageDTO> history;

    @Data
    public static class ChatMessageDTO {
        private String role;  // "user" or "assistant"
        private String content;
    }
}
