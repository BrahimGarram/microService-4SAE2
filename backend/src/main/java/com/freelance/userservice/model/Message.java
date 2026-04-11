package com.freelance.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Message Entity
 * Represents a message between two users
 */
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"sender", "receiver"})
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * User who sent the message
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    /**
     * User who received the message
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;
    
    /**
     * Message subject (optional)
     */
    @Column(length = 200)
    private String subject;
    
    /**
     * Message content
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    /**
     * Whether the message has been read
     */
    @Column(nullable = false)
    private Boolean isRead = false;
    
    /**
     * Timestamp when message was sent
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;
    
    /**
     * Timestamp when message was read
     */
    private LocalDateTime readAt;
    
    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
    }
}
