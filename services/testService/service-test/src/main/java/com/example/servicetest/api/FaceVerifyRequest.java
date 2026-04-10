package com.example.servicetest.api;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to compare two faces (profile photo + video capture).
 * Images are sent as base64 (with or without data:image/...;base64, prefix).
 */
public record FaceVerifyRequest(
        @NotBlank(message = "sourceImageBase64 is required (profile photo)")
        String sourceImageBase64,

        @NotBlank(message = "targetImageBase64 is required (video capture)")
        String targetImageBase64
) {
}

