package com.freelance.userservice.service;

import com.freelance.userservice.dto.*;
import com.freelance.userservice.exception.ResourceNotFoundException;
import com.freelance.userservice.model.*;
import com.freelance.userservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GroupChatService {

    private final GroupChatRepository groupChatRepository;
    private final GroupChatMemberRepository memberRepository;
    private final GroupChatInvitationRepository invitationRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final UserRepository userRepository;

    /**
     * Create a group chat and send invitations to the selected users.
     * Creator is automatically a member. Invitees get PENDING invitations.
     */
    public GroupChatDTO createGroupChat(Long creatorId, CreateGroupChatRequest request) {
        User creator = userRepository.findById(creatorId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + creatorId));

        GroupChat group = new GroupChat();
        group.setName(request.getName() != null && !request.getName().isBlank()
            ? request.getName().trim() : "Group chat");
        group.setCreatedBy(creator);
        group = groupChatRepository.save(group);

        GroupChatMember creatorMember = new GroupChatMember();
        creatorMember.setGroupChat(group);
        creatorMember.setUser(creator);
        memberRepository.save(creatorMember);

        if (request.getInviteeIds() != null) {
            for (Long inviteeId : request.getInviteeIds()) {
                if (inviteeId.equals(creatorId)) continue;
                User invitee = userRepository.findById(inviteeId).orElse(null);
                if (invitee == null) continue;
                if (memberRepository.existsByGroupChatAndUser(group, invitee)) continue;
                GroupChatInvitation inv = new GroupChatInvitation();
                inv.setGroupChat(group);
                inv.setInviter(creator);
                inv.setInvitee(invitee);
                inv.setStatus(GroupChatInvitation.InvitationStatus.PENDING);
                invitationRepository.save(inv);
            }
        }

        return toGroupChatDTO(group);
    }

    /**
     * List group chats the current user is a member of.
     */
    @Transactional(readOnly = true)
    public List<GroupChatDTO> getMyGroupChats(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        List<GroupChatMember> memberships = memberRepository.findByUserOrderByJoinedAtDesc(user);
        return memberships.stream()
            .map(m -> toGroupChatDTO(m.getGroupChat()))
            .collect(Collectors.toList());
    }

    /**
     * Get a single group chat by id (only if current user is a member).
     */
    @Transactional(readOnly = true)
    public GroupChatDTO getGroupChat(Long groupChatId, Long userId) {
        GroupChat group = groupChatRepository.findById(groupChatId)
            .orElseThrow(() -> new ResourceNotFoundException("Group chat not found: " + groupChatId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (!memberRepository.existsByGroupChatAndUser(group, user)) {
            throw new IllegalArgumentException("You are not a member of this group");
        }
        return toGroupChatDTO(group);
    }

    /**
     * Pending invitations for the current user.
     */
    @Transactional(readOnly = true)
    public List<GroupChatInvitationDTO> getMyPendingInvitations(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        List<GroupChatInvitation> list = invitationRepository.findByInviteeAndStatus(user, GroupChatInvitation.InvitationStatus.PENDING);
        return list.stream().map(this::toInvitationDTO).collect(Collectors.toList());
    }

    /**
     * Accept an invitation: add user as member, set status ACCEPTED.
     */
    public GroupChatDTO acceptInvitation(Long invitationId, Long userId) {
        GroupChatInvitation inv = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new ResourceNotFoundException("Invitation not found: " + invitationId));
        if (!inv.getInvitee().getId().equals(userId)) {
            throw new IllegalArgumentException("This invitation is not for you");
        }
        if (inv.getStatus() != GroupChatInvitation.InvitationStatus.PENDING) {
            throw new IllegalArgumentException("Invitation already " + inv.getStatus());
        }
        inv.setStatus(GroupChatInvitation.InvitationStatus.ACCEPTED);
        invitationRepository.save(inv);

        GroupChatMember member = new GroupChatMember();
        member.setGroupChat(inv.getGroupChat());
        member.setUser(inv.getInvitee());
        memberRepository.save(member);

        log.info("User {} accepted invitation to group {}", userId, inv.getGroupChat().getId());
        return toGroupChatDTO(inv.getGroupChat());
    }

    /**
     * Decline an invitation.
     */
    public void declineInvitation(Long invitationId, Long userId) {
        GroupChatInvitation inv = invitationRepository.findById(invitationId)
            .orElseThrow(() -> new ResourceNotFoundException("Invitation not found: " + invitationId));
        if (!inv.getInvitee().getId().equals(userId)) {
            throw new IllegalArgumentException("This invitation is not for you");
        }
        if (inv.getStatus() != GroupChatInvitation.InvitationStatus.PENDING) {
            return;
        }
        inv.setStatus(GroupChatInvitation.InvitationStatus.DECLINED);
        invitationRepository.save(inv);
        log.info("User {} declined invitation to group {}", userId, inv.getGroupChat().getId());
    }

    /**
     * Get messages for a group (only if current user is a member).
     */
    @Transactional(readOnly = true)
    public List<GroupMessageDTO> getGroupMessages(Long groupChatId, Long userId) {
        GroupChat group = groupChatRepository.findById(groupChatId)
            .orElseThrow(() -> new ResourceNotFoundException("Group chat not found: " + groupChatId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (!memberRepository.existsByGroupChatAndUser(group, user)) {
            throw new IllegalArgumentException("You are not a member of this group");
        }
        return groupMessageRepository.findByGroupChatOrderBySentAtAsc(group).stream()
            .map(this::toMessageDTO)
            .collect(Collectors.toList());
    }

    /**
     * Send a message in a group (only members can send).
     */
    public GroupMessageDTO sendGroupMessage(Long userId, SendGroupMessageRequest request) {
        GroupChat group = groupChatRepository.findById(request.getGroupChatId())
            .orElseThrow(() -> new ResourceNotFoundException("Group chat not found: " + request.getGroupChatId()));
        User sender = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (!memberRepository.existsByGroupChatAndUser(group, sender)) {
            throw new IllegalArgumentException("You are not a member of this group");
        }
        GroupMessage msg = new GroupMessage();
        msg.setGroupChat(group);
        msg.setSender(sender);
        msg.setContent(request.getContent().trim());
        msg = groupMessageRepository.save(msg);
        return toMessageDTO(msg);
    }

    private GroupChatDTO toGroupChatDTO(GroupChat group) {
        GroupChatDTO dto = new GroupChatDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setCreatedById(group.getCreatedBy().getId());
        dto.setCreatedByUsername(group.getCreatedBy().getUsername());
        dto.setCreatedAt(group.getCreatedAt());
        List<GroupChatMember> members = memberRepository.findByGroupChatOrderByJoinedAtAsc(group);
        dto.setMembers(members.stream()
            .map(m -> new GroupChatMemberDTO(m.getUser().getId(), m.getUser().getUsername()))
            .collect(Collectors.toList()));
        return dto;
    }

    private GroupChatInvitationDTO toInvitationDTO(GroupChatInvitation inv) {
        GroupChatInvitationDTO dto = new GroupChatInvitationDTO();
        dto.setId(inv.getId());
        dto.setGroupChatId(inv.getGroupChat().getId());
        dto.setGroupChatName(inv.getGroupChat().getName());
        dto.setInviterId(inv.getInviter().getId());
        dto.setInviterUsername(inv.getInviter().getUsername());
        dto.setInviteeId(inv.getInvitee().getId());
        dto.setStatus(inv.getStatus().name());
        dto.setCreatedAt(inv.getCreatedAt());
        return dto;
    }

    private GroupMessageDTO toMessageDTO(GroupMessage m) {
        GroupMessageDTO dto = new GroupMessageDTO();
        dto.setId(m.getId());
        dto.setGroupChatId(m.getGroupChat().getId());
        dto.setSenderId(m.getSender().getId());
        dto.setSenderUsername(m.getSender().getUsername());
        dto.setContent(m.getContent());
        dto.setSentAt(m.getSentAt());
        return dto;
    }
}
