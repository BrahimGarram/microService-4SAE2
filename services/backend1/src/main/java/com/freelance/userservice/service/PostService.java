package com.freelance.userservice.service;

import com.freelance.userservice.dto.PostDTO;
import com.freelance.userservice.model.Post;
import com.freelance.userservice.model.PostLike;
import com.freelance.userservice.model.PostType;
import com.freelance.userservice.model.User;
import com.freelance.userservice.repository.PostCommentRepository;
import com.freelance.userservice.repository.PostLikeRepository;
import com.freelance.userservice.repository.PostRepository;
import com.freelance.userservice.repository.UserRepository;
import com.freelance.userservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final UserRepository userRepository;
    private final FollowService followService;

    /** Returns current user id from keycloak id, or null if not found. */
    public Long getCurrentUserId(String keycloakId) {
        if (keycloakId == null || keycloakId.isBlank()) return null;
        return userRepository.findByKeycloakId(keycloakId).map(User::getId).orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> getFeed(Pageable pageable, Long currentUserId, PostType type, String sort,
                                String search, String hashtag, boolean followingOnly) {
        Page<Post> page;
        if (followingOnly && currentUserId != null) {
            List<Long> followingIds = followService.getFollowingUserIds(currentUserId);
            if (followingIds.isEmpty()) {
                page = postRepository.findByUserIdInOrderByCreatedAtDesc(Collections.singletonList(-1L), pageable);
            } else {
                page = postRepository.findByUserIdInOrderByCreatedAtDesc(followingIds, pageable);
            }
        } else if (hashtag != null && !hashtag.isBlank()) {
            String tag = hashtag.startsWith("#") ? hashtag : "#" + hashtag;
            page = postRepository.findByHashtag(tag, pageable);
        } else if (search != null && !search.isBlank()) {
            page = postRepository.searchByCaption(search, pageable);
        } else if (type != null) {
            page = postRepository.findByTypeOrderByCreatedAtDesc(type, pageable);
        } else if ("popular".equalsIgnoreCase(sort)) {
            page = postRepository.findAllOrderByLikesDesc(pageable);
        } else {
            page = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
        return page.map(p -> toDTO(p, currentUser));
    }

    @Transactional(readOnly = true)
    public PostDTO getPostById(Long postId, Long currentUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
        return toDTO(post, currentUser);
    }

    @Transactional
    public PostDTO createPost(String keycloakId, PostType type, String caption, String mediaUrl, List<String> mediaUrls) {
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Post post = new Post();
        post.setUser(user);
        post.setType(type);
        post.setCaption(caption);
        post.setMediaUrl(mediaUrl);
        if (mediaUrls != null && !mediaUrls.isEmpty()) {
            post.setMediaUrls(mediaUrls);
        }
        post = postRepository.save(post);
        log.info("Post created: id={}, user={}", post.getId(), user.getUsername());
        return toDTO(post, user);
    }

    @Transactional
    public void like(Long postId, String keycloakId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (postLikeRepository.existsByPostAndUser(post, user)) {
            return;
        }
        PostLike like = new PostLike();
        like.setPost(post);
        like.setUser(user);
        postLikeRepository.save(like);
        log.debug("Post {} liked by user {}", postId, user.getUsername());
    }

    @Transactional
    public void unlike(Long postId, String keycloakId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        postLikeRepository.deleteByPostAndUser(post, user);
        log.debug("Post {} unliked by user {}", postId, user.getUsername());
    }

    private PostDTO toDTO(Post post, User currentUser) {
        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setUserId(post.getUser().getId());
        dto.setUsername(post.getUser().getUsername());
        dto.setUserProfilePictureUrl(post.getUser().getProfilePicture());
        dto.setType(post.getType());
        dto.setMediaUrl(post.getMediaUrl());
        if (post.getMediaUrls() != null && !post.getMediaUrls().isEmpty()) {
            dto.setMediaUrls(new ArrayList<>(post.getMediaUrls()));
        } else if (post.getMediaUrl() != null) {
            dto.setMediaUrls(Collections.singletonList(post.getMediaUrl()));
        }
        dto.setCaption(post.getCaption());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setLikeCount(postLikeRepository.countByPost(post));
        dto.setCommentCount(postCommentRepository.countByPost(post));
        dto.setLikedByCurrentUser(currentUser != null && postLikeRepository.existsByPostAndUser(post, currentUser));
        return dto;
    }
}
