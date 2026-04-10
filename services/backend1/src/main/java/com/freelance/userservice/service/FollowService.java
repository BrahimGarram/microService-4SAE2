package com.freelance.userservice.service;

import com.freelance.userservice.model.Follow;
import com.freelance.userservice.model.User;
import com.freelance.userservice.repository.FollowRepository;
import com.freelance.userservice.repository.UserRepository;
import com.freelance.userservice.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Long> getFollowingUserIds(Long followerUserId) {
        User follower = userRepository.findById(followerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return followRepository.findFollowingUserIdsByFollower(follower);
    }

    @Transactional
    public void follow(Long followingUserId, String followerKeycloakId) {
        User follower = userRepository.findByKeycloakId(followerKeycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User following = userRepository.findById(followingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (follower.getId().equals(following.getId())) return;
        if (followRepository.existsByFollowerAndFollowing(follower, following)) return;
        Follow f = new Follow();
        f.setFollower(follower);
        f.setFollowing(following);
        followRepository.save(f);
    }

    @Transactional
    public void unfollow(Long followingUserId, String followerKeycloakId) {
        User follower = userRepository.findByKeycloakId(followerKeycloakId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        User following = userRepository.findById(followingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        followRepository.deleteByFollowerAndFollowing(follower, following);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerUserId, Long followingUserId) {
        User follower = userRepository.findById(followerUserId).orElse(null);
        User following = userRepository.findById(followingUserId).orElse(null);
        if (follower == null || following == null) return false;
        return followRepository.existsByFollowerAndFollowing(follower, following);
    }

    @Transactional(readOnly = true)
    public long getFollowerCount(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user == null ? 0L : followRepository.countByFollowing(user);
    }

    @Transactional(readOnly = true)
    public long getFollowingCount(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user == null ? 0L : followRepository.countByFollower(user);
    }

    @Transactional(readOnly = true)
    public List<Long> getFollowerUserIds(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return followRepository.findFollowerIdsByFollowing(user);
    }
}
