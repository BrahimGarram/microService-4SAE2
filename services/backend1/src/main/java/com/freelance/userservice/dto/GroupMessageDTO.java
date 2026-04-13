package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageDTO {
    private Long id;
    private Long groupChatId;
    private Long senderId;
    private String senderUsername;
    private String content;
    private LocalDateTime sentAt;
}
