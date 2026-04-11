package com.freelance.userservice.repository;

import com.freelance.userservice.model.Post;
import com.freelance.userservice.model.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Post> findByTypeOrderByCreatedAtDesc(PostType type, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE LOWER(p.caption) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY p.createdAt DESC")
    Page<Post> searchByCaption(@Param("q") String q, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE LOWER(p.caption) LIKE LOWER(CONCAT('%', :tag, '%')) ORDER BY p.createdAt DESC")
    Page<Post> findByHashtag(@Param("tag") String tag, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.user.id IN :userIds ORDER BY p.createdAt DESC")
    Page<Post> findByUserIdInOrderByCreatedAtDesc(@Param("userIds") List<Long> userIds, Pageable pageable);

    @Query(value = "SELECT p.* FROM posts p LEFT JOIN (SELECT post_id, COUNT(*) as cnt FROM post_likes GROUP BY post_id) pl ON p.id = pl.post_id ORDER BY COALESCE(pl.cnt, 0) DESC, p.created_at DESC", countQuery = "SELECT COUNT(*) FROM posts p", nativeQuery = true)
    Page<Post> findAllOrderByLikesDesc(Pageable pageable);

    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
