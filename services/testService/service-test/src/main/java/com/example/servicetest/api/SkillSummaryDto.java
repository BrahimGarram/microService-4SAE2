package com.example.servicetest.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Minimal skill view from user-service (only name is used for test domains).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillSummaryDto {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
