package com.freelance.userservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {

    @NotNull
    private Long targetUserId;

    @NotNull
    @Size(max = 50)
    private String type;

    @NotNull
    @Size(max = 500)
    private String message;

    private Long relatedId;
}
