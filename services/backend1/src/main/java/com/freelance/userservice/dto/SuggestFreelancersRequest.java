package com.freelance.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for AI freelancer suggestion: user describes what they need.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuggestFreelancersRequest {

    @NotBlank(message = "Message is required")
    private String message;
}
