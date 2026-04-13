package com.freelance.userservice.service;

import com.freelance.userservice.dto.MessageDTO;
import com.freelance.userservice.dto.SendMessageRequest;
import com.freelance.userservice.exception.ResourceNotFoundException;
import com.freelance.userservice.model.Message;
import com.freelance.userservice.model.User;
import com.freelance.userservice.repository.MessageRepository;
import com.freelance.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Message Service
 * Handles messaging between users
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MessageService {
    
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    
    /**
     * Send a message from one user to another
     * @param senderId ID of user sending the message
     * @param request SendMessageRequest with receiver ID and content
     * @return MessageDTO
     */
    public MessageDTO sendMessage(Long senderId, SendMessageRequest request) {
        // Prevent self-messaging
        if (senderId.equals(request.getReceiverId())) {
            throw new IllegalArgumentException("You cannot send a message to yourself");
        }
        
        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new ResourceNotFoundException("Sender not found with id: " + senderId));
        
        User receiver = userRepository.findById(request.getReceiverId())
            .orElseThrow(() -> new ResourceNotFoundException("Receiver not found with id: " + request.getReceiverId()));
        
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSubject(request.getSubject());
        message.setContent(request.getContent());
        message.setIsRead(false);
        if (request.getProposalId() != null) {
            message.setProposalId(request.getProposalId());
        }
        if (request.getProjectId() != null) {
            message.setProjectId(request.getProjectId());
        }

        Message savedMessage = messageRepository.save(message);
        log.info("Message sent from user {} to user {}", senderId, request.getReceiverId());
        
        return convertToDTO(savedMessage);
    }
    
    /**
     * Get messages for a project room (only messages where current user is sender or receiver)
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getMessagesByProject(Long projectId, Long userId) {
        List<Message> messages = messageRepository.findByProjectIdAndParticipantOrderBySentAtAsc(projectId, userId);
        return messages.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get all messages for a project room (admin only - no participant filter)
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getMessagesByProjectAsAdmin(Long projectId) {
        List<Message> messages = messageRepository.findByProjectIdOrderBySentAtAsc(projectId);
        return messages.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get conversation between two users
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return List of MessageDTO
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getConversation(Long userId1, Long userId2) {
        User user1 = userRepository.findById(userId1)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId1));
        
        User user2 = userRepository.findById(userId2)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId2));
        
        List<Message> messages = messageRepository.findConversation(user1, user2);
        return messages.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all conversations for a user (list of users they've messaged with)
     * @param userId User ID
     * @return List of User IDs that the user has conversations with
     */
    @Transactional(readOnly = true)
    public List<Long> getConversationPartners(Long userId) {
        // Use the optimized query that directly returns user IDs
        List<Long> partnerIds = messageRepository.findConversationPartnerIds(userId);
        log.debug("Found {} conversation partners for user {}", partnerIds.size(), userId);
        return partnerIds;
    }
    
    /**
     * Get all messages received by a user
     * @param userId User ID
     * @return List of MessageDTO
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getReceivedMessages(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<Message> messages = messageRepository.findByReceiverOrderBySentAtDesc(user);
        return messages.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all messages sent by a user
     * @param userId User ID
     * @return List of MessageDTO
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getSentMessages(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<Message> messages = messageRepository.findBySenderOrderBySentAtDesc(user);
        return messages.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get unread messages count for a user
     * @param userId User ID
     * @return Count of unread messages
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        return messageRepository.countByReceiverAndIsReadFalse(user);
    }
    
    /**
     * Get unread messages for a user
     * @param userId User ID
     * @return List of unread MessageDTO
     */
    @Transactional(readOnly = true)
    public List<MessageDTO> getUnreadMessages(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        List<Message> messages = messageRepository.findByReceiverAndIsReadFalseOrderBySentAtDesc(user);
        return messages.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Mark a message as read
     * @param messageId Message ID
     * @param userId User ID (must be the receiver)
     * @return MessageDTO
     */
    public MessageDTO markAsRead(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));
        
        if (!message.getReceiver().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only mark your own received messages as read");
        }
        
        message.setIsRead(true);
        message.setReadAt(LocalDateTime.now());
        
        Message savedMessage = messageRepository.save(message);
        return convertToDTO(savedMessage);
    }
    
    /**
     * Mark all messages from a conversation as read
     * @param userId Current user ID
     * @param otherUserId Other user ID in the conversation
     */
    public void markConversationAsRead(Long userId, Long otherUserId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        User otherUser = userRepository.findById(otherUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + otherUserId));
        
        List<Message> messages = messageRepository.findConversation(user, otherUser);
        LocalDateTime now = LocalDateTime.now();
        
        for (Message message : messages) {
            if (message.getReceiver().getId().equals(userId) && !message.getIsRead()) {
                message.setIsRead(true);
                message.setReadAt(now);
            }
        }
        
        messageRepository.saveAll(messages);
        log.info("Conversation marked as read between user {} and user {}", userId, otherUserId);
    }
    
    /**
     * Delete a message
     * @param messageId Message ID
     * @param userId User ID (must be sender or receiver)
     */
    public void deleteMessage(Long messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));
        
        if (!message.getSender().getId().equals(userId) && !message.getReceiver().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only delete your own messages");
        }
        
        messageRepository.delete(message);
        log.info("Message {} deleted by user {}", messageId, userId);
    }
    
    /**
     * Convert Message entity to MessageDTO
     */
    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderUsername(message.getSender().getUsername());
        dto.setReceiverId(message.getReceiver().getId());
        dto.setReceiverUsername(message.getReceiver().getUsername());
        dto.setSubject(message.getSubject());
        dto.setContent(message.getContent());
        dto.setIsRead(message.getIsRead());
        dto.setSentAt(message.getSentAt());
        dto.setReadAt(message.getReadAt());
        dto.setProposalId(message.getProposalId());
        dto.setProjectId(message.getProjectId());
        return dto;
    }
}
