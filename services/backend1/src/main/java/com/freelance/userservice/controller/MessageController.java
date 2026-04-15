package com.freelance.userservice.controller;

import com.freelance.userservice.dto.MessageDTO;
import com.freelance.userservice.dto.SendMessageRequest;
import com.freelance.userservice.dto.UserDTO;
import com.freelance.userservice.service.MessageService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message REST Controller
 * 
 * Provides REST endpoints for messaging between users.
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    
    private final MessageService messageService;
    private final UserService userService;
    
    /**
     * POST /api/messages
     * 
     * Send a message to another user.
     * 
     * @param request SendMessageRequest with receiver ID and content
     * @param authentication Spring Security authentication object
     * @return Created MessageDTO
     */
    @PostMapping
    public ResponseEntity<MessageDTO> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication) {
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserKeycloakId = JwtUtil.extractKeycloakId(jwt);
        UserDTO currentUser = userService.getCurrentUser(currentUserKeycloakId);
        
        MessageDTO message = messageService.sendMessage(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
    
    /**
     * GET /api/messages/project/{projectId}
     *
     * Get all messages for a project room (client, freelancer and admin).
     * Everyone with access to the dashboard sees the same full thread
     * (including messages between admin and client, admin and freelancer).
     * Access to the project dashboard is enforced by the frontend (only
     * owner, assigned freelancer or admin can open the room).
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<MessageDTO>> getProjectMessages(
            @PathVariable Long projectId,
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserKeycloakId = JwtUtil.extractKeycloakId(jwt);
        userService.getCurrentUser(currentUserKeycloakId);

        List<MessageDTO> messages = messageService.getMessagesByProjectAsAdmin(projectId);
        return ResponseEntity.ok(messages);
    }

    /**
     * GET /api/messages/conversation/{userId}
     * 
     * Get conversation between current user and another user.
     * 
     * @param userId Other user ID
     * @param authentication Spring Security authentication object
     * @return List of MessageDTO
     */
    @GetMapping("/conversation/{userId}")
    public ResponseEntity<List<MessageDTO>> getConversation(
            @PathVariable Long userId,
            Authentication authentication) {
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserKeycloakId = JwtUtil.extractKeycloakId(jwt);
        UserDTO currentUser = userService.getCurrentUser(currentUserKeycloakId);
        
        List<MessageDTO> messages = messageService.getConversation(currentUser.getId(), userId);
        return ResponseEntity.ok(messages);
    }
    
    /**
     * GET /api/messages/conversations
     * 
     * Get list of users that current user has conversations with.
     * 
     * @param authentication Spring Security authentication object
     * @return List of user IDs
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<Long>> getConversations(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserKeycloakId = JwtUtil.extractKeycloakId(jwt);
        UserDTO currentUser = userService.getCurrentUser(currentUserKeycloakId);
        
        List<Long> conversationPartners = messageService.getConversationPartners(currentUser.getId());
        return ResponseEntity.ok(conversationPartners);
    }
    
    /**
     * GET /api/messages/received
     * 
     * Get all messages received by current user.
     * 
     * @param authentication Spring Security authentication object
     * @return List of MessageDTO
     */
    @GetMapping("/received")
    public ResponseEntity<List<MessageDTO>> getReceivedMessages(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserKeycloakId = JwtUtil.extractKeycloakId(jwt);
        UserDTO currentUser = userService.getCurrentUser(currentUserKeycloakId);
        
        List<MessageDTO> messages = messageService.getReceivedMessages(currentUser.getId());
        return ResponseEntity.ok(messages);
    }
    
    /**
     * GET /api/messages/sent
     * 
     * Get all messages sent by current user.
     * 
     * @param authentication Spring Security authentication object
     * @return List of MessageDTO
     */
    @GetMapping("/sent")
    public ResponseEntity<List<MessageDTO>> getSentMessages(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserKeycloakId = JwtUtil.extractKeycloakId(jwt);
        UserDTO currentUser = userService.getCurrentUser(currentUserKeycloakId);
        
        List<MessageDTO> messages = messageService.getSentMessages(currentUser.getId());
        return ResponseEntity.ok(messages);
    }
    
    /**
     * GET /api/messages/unread
     * 
     * Get unread messages count and list for current user.
     * 
     * @param authentication Spring Security authentication object
     * @return Map with unread count and messages
     */
    @GetMapping("/unread")
    public ResponseEntity<Map<String, Object>> getUnreadMessages(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserKeycloakId = JwtUtil.extractKeycloakId(jwt);
        UserDTO currentUser = userService.getCurrentUser(currentUserKeycloakId);
        
        long unreadCount = messageService.getUnreadCount(currentUser.getId());
        List<MessageDTO> unreadMessages = messageService.getUnreadMessages(currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", unreadCount);
        response.put("messages", unreadMessages);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * PUT /api/messages/{messageId}/read
     * 
     * Mark a message as read.
     * 
     * @param messageId Message ID
     * @param authentication Spring Security authentication object
     * @return Updated MessageDTO
     */
    @PutMapping("/{messageId}/read")
    public ResponseEntity<MessageDTO> markAsRead(
            @PathVariable Long messageId,
            Authentication authentication) {
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserKeycloakId = JwtUtil.extractKeycloakId(jwt);
        UserDTO currentUser = userService.getCurrentUser(currentUserKeycloakId);
        
        MessageDTO message = messageService.markAsRead(messageId, currentUser.getId());
        return ResponseEntity.ok(message);
    }
    
    /**
     * PUT /api/messages/conversation/{userId}/read
     * 
     * Mark all messages in a conversation as read.
     * 
     * @param userId Other user ID in the conversation
     * @param authentication Spring Security authentication object
     * @return Success message
     */
    @PutMapping("/conversation/{userId}/read")
    public ResponseEntity<Map<String, String>> markConversationAsRead(
            @PathVariable Long userId,
            Authentication authentication) {
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserKeycloakId = JwtUtil.extractKeycloakId(jwt);
        UserDTO currentUser = userService.getCurrentUser(currentUserKeycloakId);
        
        messageService.markConversationAsRead(currentUser.getId(), userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Conversation marked as read");
        return ResponseEntity.ok(response);
    }
    
    /**
     * DELETE /api/messages/{messageId}
     * 
     * Delete a message.
     * Users can only delete their own messages (sent or received).
     * 
     * @param messageId Message ID
     * @param authentication Spring Security authentication object
     * @return No content response
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication) {
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserKeycloakId = JwtUtil.extractKeycloakId(jwt);
        UserDTO currentUser = userService.getCurrentUser(currentUserKeycloakId);
        
        messageService.deleteMessage(messageId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
