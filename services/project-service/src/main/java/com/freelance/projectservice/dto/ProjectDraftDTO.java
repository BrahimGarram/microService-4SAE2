package com.freelance.projectservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * AI-generated project draft to pre-fill the create project form.
 * No id, no ownerId (user fills when submitting).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDraftDTO {

    private String title;
    private String description;
    private String category;
    private String tags;
    /** Suggested budget (can be null if LLM could not infer). */
    private BigDecimal suggestedBudget;
}
