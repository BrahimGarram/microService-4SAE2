package com.freelance.projectservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for AI draft-from-text: user describes project in natural language.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DraftFromTextRequest {

    @NotBlank(message = "User message is required")
    private String userMessage;
}
