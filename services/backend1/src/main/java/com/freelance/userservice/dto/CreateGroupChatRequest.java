package com.freelance.userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupChatRequest {
    private String name;
    @NotNull(message = "At least one invitee is required")
    private List<Long> inviteeIds;
}
