package com.freelance.userservice.repository;

import com.freelance.userservice.model.Post;
import com.freelance.userservice.model.PostLike;
import com.freelance.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    long countByPost(Post post);

    boolean existsByPostAndUser(Post post, User user);

    Optional<PostLike> findByPostAndUser(Post post, User user);

    void deleteByPostAndUser(Post post, User user);
}
