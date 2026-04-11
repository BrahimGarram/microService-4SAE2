package com.freelance.userservice.service;

import com.freelance.userservice.dto.PostCommentDTO;
import com.freelance.userservice.model.Post;
import com.freelance.userservice.model.PostComment;
import com.freelance.userservice.model.User;
import com.freelance.userservice.repository.PostCommentRepository;
import com.freelance.userservice.repository.PostRepository;
import com.freelance.userservice.repository.UserRepository;
import com.freelance.userservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostCommentService {

    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<PostCommentDTO> getComments(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        return commentRepository.findByPostOrderByCreatedAtAsc(post, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public long getCommentCount(Post post) {
        return commentRepository.countByPost(post);
    }

    @Transactional
    public PostCommentDTO addComment(Long postId, String keycloakId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Comment content is required");
        }
        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content.trim());
        comment = commentRepository.save(comment);
        return toDTO(comment);
    }

    private PostCommentDTO toDTO(PostComment c) {
        PostCommentDTO dto = new PostCommentDTO();
        dto.setId(c.getId());
        dto.setPostId(c.getPost().getId());
        dto.setUserId(c.getUser().getId());
        dto.setUsername(c.getUser().getUsername());
        dto.setUserProfilePictureUrl(c.getUser().getProfilePicture());
        dto.setContent(c.getContent());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }
}
