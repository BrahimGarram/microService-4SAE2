package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LanguageDTO {
    private Long id;
    private String name;
    private String code;
    private String proficiency;
}
