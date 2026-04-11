package com.freelance.userservice.repository;

import com.freelance.userservice.model.Message;
import com.freelance.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * Find all messages between two users (conversation)
     */
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender = :user1 AND m.receiver = :user2) OR " +
           "(m.sender = :user2 AND m.receiver = :user1) " +
           "ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("user1") User user1, @Param("user2") User user2);
    
    /**
     * Find all messages sent by a user
     */
    List<Message> findBySenderOrderBySentAtDesc(User sender);
    
    /**
     * Find all messages received by a user
     */
    List<Message> findByReceiverOrderBySentAtDesc(User receiver);
    
    /**
     * Find all conversations for a user (get unique users they've messaged with)
     * Returns distinct user IDs that the user has conversations with
     */
    @Query("SELECT DISTINCT CASE " +
           "WHEN m.sender.id = :userId THEN m.receiver.id " +
           "ELSE m.sender.id " +
           "END FROM Message m " +
           "WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    List<Long> findConversationPartnerIds(@Param("userId") Long userId);
    
    /**
     * Find all conversations for a user (get unique users they've messaged with)
     * Returns User entities
     */
    @Query("SELECT DISTINCT CASE " +
           "WHEN m.sender = :user THEN m.receiver " +
           "ELSE m.sender " +
           "END FROM Message m " +
           "WHERE m.sender = :user OR m.receiver = :user")
    List<User> findConversationPartners(@Param("user") User user);
    
    /**
     * Count unread messages for a user
     */
    long countByReceiverAndIsReadFalse(User receiver);
    
    /**
     * Find unread messages for a user
     */
    List<Message> findByReceiverAndIsReadFalseOrderBySentAtDesc(User receiver);
}
