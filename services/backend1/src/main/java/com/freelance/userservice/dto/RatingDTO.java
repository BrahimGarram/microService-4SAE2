package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingDTO {
    private Long id;
    private Long raterId;
    private String raterUsername;
    private Long ratedUserId;
    private Integer rating; // 1 to 5
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
