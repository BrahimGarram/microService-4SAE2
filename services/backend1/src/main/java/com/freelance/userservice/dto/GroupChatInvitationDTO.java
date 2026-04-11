package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatInvitationDTO {
    private Long id;
    private Long groupChatId;
    private String groupChatName;
    private Long inviterId;
    private String inviterUsername;
    private Long inviteeId;
    private String status; // PENDING, ACCEPTED, DECLINED
    private LocalDateTime createdAt;
}
