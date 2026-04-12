package com.freelance.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupChatDTO {
    private Long id;
    private String name;
    private Long createdById;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private List<GroupChatMemberDTO> members;
}
