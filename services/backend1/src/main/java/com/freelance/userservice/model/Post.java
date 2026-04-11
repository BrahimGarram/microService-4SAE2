package com.freelance.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Post entity for TikTok-style feed.
 * Users can publish video, image, or text posts.
 */
@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType type;

    /** URL of the media file (video or image), null for TEXT-only posts. Kept for single-media posts. */
    @Column(length = 1024)
    private String mediaUrl;

    /** Multiple media URLs for carousel (images). Order preserved. */
    @ElementCollection
    @CollectionTable(name = "post_media_urls", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "url", length = 1024)
    @OrderColumn(name = "position")
    private List<String> mediaUrls = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String caption;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
