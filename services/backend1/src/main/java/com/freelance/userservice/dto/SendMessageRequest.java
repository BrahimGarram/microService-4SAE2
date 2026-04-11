package com.freelance.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    
    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    private String subject;

    @NotBlank(message = "Message content is required")
    private String content;

    /** Optional: when sending a proposal alert, set proposalId so receiver can accept/refuse from inbox */
    private Long proposalId;

    /** Optional: project linked to the proposal */
    private Long projectId;
}
