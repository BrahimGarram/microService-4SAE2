package com.freelance.userservice.dto;

import com.freelance.userservice.model.PostType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {
    private Long id;
    private Long userId;
    private String username;
    private String userProfilePictureUrl;
    private PostType type;
    private String mediaUrl;
    private List<String> mediaUrls;
    private String caption;
    private LocalDateTime createdAt;
    private long likeCount;
    private long commentCount;
    private boolean likedByCurrentUser;
}
