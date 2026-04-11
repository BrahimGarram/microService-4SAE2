package com.example.servicetest.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Minimal view of a freelancer coming from user-service.
 * Maps the fields exposed by UserDTO in user-service.
 * Skills are used to determine test domains (when starting a test).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSummaryDto {

    private Long id;
    private String username;
    private List<SkillSummaryDto> skills;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<SkillSummaryDto> getSkills() {
        return skills;
    }

    public void setSkills(List<SkillSummaryDto> skills) {
        this.skills = skills;
    }
}


