package com.freelance.collaborationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDTO {
    private Long id;
    private String name;
    private Long ownerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
