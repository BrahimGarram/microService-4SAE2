package com.freelance.userservice.controller;

import com.freelance.userservice.dto.CreatePostCommentRequest;
import com.freelance.userservice.dto.PostCommentDTO;
import com.freelance.userservice.dto.PostDTO;
import com.freelance.userservice.model.PostType;
import com.freelance.userservice.service.FileStorageService;
import com.freelance.userservice.service.PostCommentService;
import com.freelance.userservice.service.PostService;
import com.freelance.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * REST controller for TikTok-style feed: posts, likes, comments, follow.
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;
    private final PostCommentService postCommentService;
    private final FileStorageService fileStorageService;

    @GetMapping("/feed")
    public ResponseEntity<Page<PostDTO>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) PostType type,
            @RequestParam(required = false, defaultValue = "recent") String sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String hashtag,
            @RequestParam(required = false, defaultValue = "false") boolean followingOnly,
            Authentication authentication) {
        String keycloakId = authentication != null ? JwtUtil.extractKeycloakId((Jwt) authentication.getPrincipal()) : null;
        Long currentUserId = postService.getCurrentUserId(keycloakId);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postService.getFeed(pageable, currentUserId, type, sort, search, hashtag, followingOnly));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPost(@PathVariable Long id, Authentication authentication) {
        String keycloakId = authentication != null ? JwtUtil.extractKeycloakId((Jwt) authentication.getPrincipal()) : null;
        Long currentUserId = postService.getCurrentUserId(keycloakId);
        return ResponseEntity.ok(postService.getPostById(id, currentUserId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDTO> createPost(
            @RequestParam PostType type,
            @RequestParam(required = false) String caption,
            @RequestParam(required = false) MultipartFile file,
            Authentication authentication) throws IOException {
        String keycloakId = JwtUtil.extractKeycloakId((Jwt) authentication.getPrincipal());
        Long userId = postService.getCurrentUserId(keycloakId);
        String mediaUrl = null;
        if (file != null && !file.isEmpty()) {
            if (type != PostType.VIDEO && type != PostType.IMAGE) {
                return ResponseEntity.badRequest().build();
            }
            mediaUrl = fileStorageService.uploadPostMedia(file, userId);
        } else if (type == PostType.VIDEO || type == PostType.IMAGE) {
            return ResponseEntity.badRequest().build();
        }
        PostDTO created = postService.createPost(keycloakId, type, caption, mediaUrl, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<Page<PostCommentDTO>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(postCommentService.getComments(id, pageable));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<PostCommentDTO> addComment(
            @PathVariable Long id,
            @RequestBody CreatePostCommentRequest request,
            Authentication authentication) {
        String keycloakId = JwtUtil.extractKeycloakId((Jwt) authentication.getPrincipal());
        PostCommentDTO created = postCommentService.addComment(id, keycloakId, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> like(@PathVariable Long id, Authentication authentication) {
        String keycloakId = JwtUtil.extractKeycloakId((Jwt) authentication.getPrincipal());
        postService.like(id, keycloakId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> unlike(@PathVariable Long id, Authentication authentication) {
        String keycloakId = JwtUtil.extractKeycloakId((Jwt) authentication.getPrincipal());
        postService.unlike(id, keycloakId);
        return ResponseEntity.ok().build();
    }
}

