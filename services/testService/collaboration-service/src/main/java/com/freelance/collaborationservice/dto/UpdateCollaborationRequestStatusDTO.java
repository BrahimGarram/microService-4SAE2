package com.freelance.collaborationservice.dto;

import com.freelance.collaborationservice.model.CollaborationRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCollaborationRequestStatusDTO {

    @NotNull(message = "Status is required")
    private CollaborationRequestStatus status;
}
