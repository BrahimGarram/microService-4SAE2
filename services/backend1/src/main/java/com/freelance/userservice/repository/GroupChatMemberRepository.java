package com.freelance.userservice.repository;

import com.freelance.userservice.model.GroupChat;
import com.freelance.userservice.model.GroupChatMember;
import com.freelance.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupChatMemberRepository extends JpaRepository<GroupChatMember, Long> {

    List<GroupChatMember> findByUserOrderByJoinedAtDesc(User user);

    @Query("SELECT m FROM GroupChatMember m WHERE m.user = :user")
    List<GroupChatMember> findByUser(@Param("user") User user);

    Optional<GroupChatMember> findByGroupChatAndUser(GroupChat groupChat, User user);

    boolean existsByGroupChatAndUser(GroupChat groupChat, User user);

    List<GroupChatMember> findByGroupChatOrderByJoinedAtAsc(GroupChat groupChat);
}
