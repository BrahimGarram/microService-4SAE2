package com.freelance.userservice.service;

import com.freelance.userservice.dto.CreateNotificationRequest;
import com.freelance.userservice.dto.NotificationDTO;
import com.freelance.userservice.exception.ResourceNotFoundException;
import com.freelance.userservice.model.Notification;
import com.freelance.userservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationDTO create(CreateNotificationRequest request, Long senderUserId) {
        Notification n = new Notification();
        n.setTargetUserId(request.getTargetUserId());
        n.setSenderUserId(senderUserId);
        n.setType(request.getType());
        n.setMessage(request.getMessage());
        n.setRelatedId(request.getRelatedId());
        n.setRead(false);
        Notification saved = notificationRepository.save(n);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getByTargetUser(Long targetUserId) {
        return notificationRepository.findByTargetUserIdOrderByCreatedAtDesc(targetUserId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long targetUserId) {
        return notificationRepository.countByTargetUserIdAndReadFalse(targetUserId);
    }

    @Transactional
    public NotificationDTO markAsRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        if (!n.getTargetUserId().equals(userId)) {
            throw new IllegalArgumentException("You can only mark your own notifications as read");
        }
        n.setRead(true);
        return toDTO(notificationRepository.save(n));
    }

    private NotificationDTO toDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .targetUserId(n.getTargetUserId())
                .senderUserId(n.getSenderUserId())
                .type(n.getType())
                .message(n.getMessage())
                .relatedId(n.getRelatedId())
                .read(n.getRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
