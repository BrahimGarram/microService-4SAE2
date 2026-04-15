package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    private Long id;
    private Long targetUserId;
    private Long senderUserId;
    private String type;
    private String message;
    private Long relatedId;
    private Boolean read;
    private LocalDateTime createdAt;
}
