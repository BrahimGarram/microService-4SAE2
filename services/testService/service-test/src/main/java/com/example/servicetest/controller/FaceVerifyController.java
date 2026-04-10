package com.example.servicetest.controller;

import com.example.servicetest.api.FaceVerifyRequest;
import com.example.servicetest.api.FaceVerifyResponse;
import com.example.servicetest.service.CompreFaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

/**
 * Face verification API (profile photo vs camera capture).
 * Delegates to CompreFace if configured, otherwise returns 503.
 */
@RestController
@RequestMapping("/api/face")
@RequiredArgsConstructor
@Slf4j
public class FaceVerifyController {

    private final CompreFaceService compreFaceService;

    @Value("${app.compreface.save-captures:false}")
    private boolean saveCaptures;

    @Value("${app.compreface.captures-dir:face-captures}")
    private String capturesDir;

    /**
     * Compare two images (profile photo vs camera capture).
     * If save-captures is enabled, the video capture is stored on disk for inspection.
     *
     * @return 200 with match + similarity when CompreFace is available, 503 otherwise
     */
    @PostMapping("/verify")
    public ResponseEntity<FaceVerifyResponse> verify(@Valid @RequestBody FaceVerifyRequest request) {
        if (saveCaptures && request.targetImageBase64() != null && !request.targetImageBase64().isBlank()) {
            saveCaptureToProject(request.targetImageBase64());
        }
        Optional<FaceVerifyResponse> result = compreFaceService.verify(
                request.sourceImageBase64(),
                request.targetImageBase64()
        );
        return result
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(FaceVerifyResponse.error("Face verification unavailable (CompreFace not configured).")));
    }

    /** Save camera capture (image sent for similarity) into project folder for debugging. */
    private void saveCaptureToProject(String targetImageBase64) {
        try {
            String base64 = targetImageBase64;
            int prefix = base64.indexOf("base64,");
            if (prefix >= 0) base64 = base64.substring(prefix + 7).trim();
            byte[] bytes = Base64.getDecoder().decode(base64);
            if (bytes.length == 0) return;
            Path dir = Path.of(capturesDir);
            Files.createDirectories(dir);
            String name = "capture_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + "_" + System.currentTimeMillis() + ".jpg";
            Path file = dir.resolve(name);
            Files.write(file, bytes);
            log.info("Verification capture stored at: {}", file.toAbsolutePath());
        } catch (Exception e) {
            log.warn("Unable to store capture: {}", e.getMessage());
        }
    }
}

