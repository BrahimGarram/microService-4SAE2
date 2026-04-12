package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioItemDTO {
    private Long id;
    private String title;
    private String description;
    private String url;
    private String imageUrl;
    private String technologies;
    private LocalDateTime completedDate;
    private Integer displayOrder;
}
