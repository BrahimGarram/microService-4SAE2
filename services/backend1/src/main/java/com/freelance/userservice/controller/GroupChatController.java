package com.freelance.userservice.controller;

import com.freelance.userservice.dto.*;
import com.freelance.userservice.service.GroupChatService;
import com.freelance.userservice.service.UserService;
import com.freelance.userservice.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group-chats")
@RequiredArgsConstructor
@Slf4j
public class GroupChatController {

    private final GroupChatService groupChatService;
    private final UserService userService;

    private Long currentUserId(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = JwtUtil.extractKeycloakId(jwt);
        return userService.getCurrentUser(keycloakId).getId();
    }

    /**
     * Create a group chat and send invitations to the selected users.
     * POST /api/group-chats
     */
    @PostMapping
    public ResponseEntity<GroupChatDTO> createGroupChat(
            @Valid @RequestBody CreateGroupChatRequest request,
            Authentication authentication) {
        Long userId = currentUserId(authentication);
        GroupChatDTO created = groupChatService.createGroupChat(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * List group chats the current user is a member of.
     * GET /api/group-chats
     */
    @GetMapping
    public ResponseEntity<List<GroupChatDTO>> getMyGroupChats(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.ok(groupChatService.getMyGroupChats(userId));
    }

    /**
     * Get pending invitations for the current user.
     * GET /api/group-chats/invitations (must be before /{id})
     */
    @GetMapping("/invitations")
    public ResponseEntity<List<GroupChatInvitationDTO>> getMyPendingInvitations(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.ok(groupChatService.getMyPendingInvitations(userId));
    }

    /**
     * Get a single group chat by id.
     * GET /api/group-chats/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<GroupChatDTO> getGroupChat(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.ok(groupChatService.getGroupChat(id, userId));
    }

    /**
     * Accept an invitation.
     * POST /api/group-chats/invitations/{invitationId}/accept
     */
    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<GroupChatDTO> acceptInvitation(
            @PathVariable Long invitationId,
            Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.ok(groupChatService.acceptInvitation(invitationId, userId));
    }

    /**
     * Decline an invitation.
     * POST /api/group-chats/invitations/{invitationId}/decline
     */
    @PostMapping("/invitations/{invitationId}/decline")
    public ResponseEntity<Void> declineInvitation(
            @PathVariable Long invitationId,
            Authentication authentication) {
        Long userId = currentUserId(authentication);
        groupChatService.declineInvitation(invitationId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get messages for a group chat.
     * GET /api/group-chats/{id}/messages
     */
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<GroupMessageDTO>> getGroupMessages(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.ok(groupChatService.getGroupMessages(id, userId));
    }

    /**
     * Send a message in a group chat.
     * POST /api/group-chats/messages
     */
    @PostMapping("/messages")
    public ResponseEntity<GroupMessageDTO> sendGroupMessage(
            @Valid @RequestBody SendGroupMessageRequest request,
            Authentication authentication) {
        Long userId = currentUserId(authentication);
        GroupMessageDTO sent = groupChatService.sendGroupMessage(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sent);
    }
}
