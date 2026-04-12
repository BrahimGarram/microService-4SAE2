package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentDTO {
    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String userProfilePictureUrl;
    private String content;
    private LocalDateTime createdAt;
}
