package com.freelance.userservice.dto;

import com.freelance.userservice.model.PostType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {
    private PostType type;
    private String caption;
    /** Optional: set after upload; if type is VIDEO or IMAGE, media is uploaded separately then URL set here or in multipart. */
    private String mediaUrl;
}
