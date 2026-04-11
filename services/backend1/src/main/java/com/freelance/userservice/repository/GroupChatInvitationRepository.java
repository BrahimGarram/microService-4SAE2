package com.freelance.userservice.repository;

import com.freelance.userservice.model.GroupChat;
import com.freelance.userservice.model.GroupChatInvitation;
import com.freelance.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupChatInvitationRepository extends JpaRepository<GroupChatInvitation, Long> {

    List<GroupChatInvitation> findByInviteeAndStatus(User invitee, GroupChatInvitation.InvitationStatus status);

    Optional<GroupChatInvitation> findByGroupChatAndInvitee(GroupChat groupChat, User invitee);

    List<GroupChatInvitation> findByGroupChat(GroupChat groupChat);
}
