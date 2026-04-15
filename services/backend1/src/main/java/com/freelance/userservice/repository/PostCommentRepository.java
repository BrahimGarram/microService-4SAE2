package com.freelance.userservice.repository;

import com.freelance.userservice.model.Post;
import com.freelance.userservice.model.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    Page<PostComment> findByPostOrderByCreatedAtAsc(Post post, Pageable pageable);

    long countByPost(Post post);
}
