package com.freelance.userservice.repository;

import com.freelance.userservice.model.User;
import com.freelance.userservice.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerAndFollowing(User follower, User following);

    void deleteByFollowerAndFollowing(User follower, User following);

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower = :follower")
    List<Long> findFollowingUserIdsByFollower(@Param("follower") User follower);

    long countByFollowing(User following);

    long countByFollower(User follower);

    @Query("SELECT f.follower.id FROM Follow f WHERE f.following = :following")
    List<Long> findFollowerIdsByFollowing(@Param("following") User following);
}
