package com.example.servicetest.service;

import com.example.servicetest.api.FaceVerifyResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Client for the CompreFace API (1:1 face verification).
 * If URL or API key are not configured, calls are ignored.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompreFaceService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.compreface.url:}")
    private String comprefaceUrl;

    @Value("${app.compreface.verification-api-key:}")
    private String verificationApiKey;

    @Value("${app.compreface.detection-api-key:}")
    private String detectionApiKey;

    /** Similarity threshold above which we consider it is the same person (0.0 - 1.0). */
    @Value("${app.compreface.similarity-threshold:0.6}")
    private double similarityThreshold;

    /** Face detection probability threshold (0.0 - 1.0). Lower = more permissive, fewer 400 \"No face is found\". */
    @Value("${app.compreface.det-prob-threshold:0.05}")
    private double detProbThreshold;

    /**
     * Compare two face images via CompreFace.
     *
     * @param sourceBase64 Image source (profile photo), base64 (with or without data:image/... prefix)
     * @param targetBase64 Image target (video capture), base64 (with or without data:image/... prefix)
     * @return Response with match and similarity if CompreFace is configured and answers, empty otherwise
     */
    public Optional<FaceVerifyResponse> verify(String sourceBase64, String targetBase64) {
        if (comprefaceUrl == null || comprefaceUrl.isBlank() || verificationApiKey == null || verificationApiKey.isBlank()) {
            log.debug("CompreFace not configured (url or api-key empty)");
            return Optional.empty();
        }
        String source = stripDataUrlPrefix(sourceBase64);
        String target = stripDataUrlPrefix(targetBase64);
        log.debug("CompreFace verify: source base64 length={}, target base64 length={}", source.length(), target.length());

        // Detection: 0 face or 2+ faces in frame = fraud (only one person allowed)
        if (detectionApiKey != null && !detectionApiKey.isBlank()) {
            int faceCount = countFacesInImage(target);
            if (faceCount == 0) {
                log.warn("CompreFace [DETECTION] 0 face in frame => fraud");
                return Optional.of(FaceVerifyResponse.noFace());
            }
            if (faceCount >= 2) {
                log.warn("CompreFace [DETECTION] {} faces in frame (max 1) => fraud", faceCount);
                return Optional.of(FaceVerifyResponse.multipleFaces());
            }
        }

        String url = comprefaceUrl.replaceAll("/$", "") + "/api/v1/verification/verify?det_prob_threshold=" + detProbThreshold;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", verificationApiKey);
            String body = objectMapper.writeValueAsString(
                    java.util.Map.of("source_image", source, "target_image", target)
            );
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String responseBody = response.getBody();
                log.debug("CompreFace response length={}", responseBody.length());
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode result = root.path("result");
                if (result.isArray() && result.size() > 0) {
                    JsonNode first = result.get(0);
                    JsonNode faceMatches = first.path("face_matches");
                    if (faceMatches.isArray() && faceMatches.size() > 0) {
                        double similarity = faceMatches.get(0).path("similarity").asDouble(0.0);
                        boolean match = similarity >= similarityThreshold;
                        log.info("CompreFace [VERIFICATION] similarity score = {} (threshold = {}) => match = {}", similarity, similarityThreshold, match);
                        return Optional.of(FaceVerifyResponse.ok(match, similarity));
                    }
                    // No face_matches / empty result: treat as no face in frame
                    log.warn("CompreFace: no face_matches => fraud (no person in frame)");
                    return Optional.of(FaceVerifyResponse.noFace());
                }
                log.warn("CompreFace: empty result => fraud (no person in frame)");
                return Optional.of(FaceVerifyResponse.noFace());
            }
        } catch (HttpStatusCodeException e) {
            String body = e.getResponseBodyAsString();
            if (e.getStatusCode().value() == 400 && body != null && body.contains("No face is found")) {
                log.warn("CompreFace 400 'No face is found' => fraud (camera hidden or no person in frame)");
                return Optional.of(FaceVerifyResponse.noFace());
            }
            log.warn("CompreFace HTTP {}: {}", e.getStatusCode(), body != null && body.length() > 200 ? body.substring(0, 200) + "..." : body);
            // Do not return noFace for general verification errors
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Error calling CompreFace: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Count number of faces in image via CompreFace Face Detection API.
     *
     * @param imageBase64 Image in base64 (with or without data:image/... prefix)
     * @return Number of faces detected, or -1 on error
     */
    private int countFacesInImage(String imageBase64) {
        if (comprefaceUrl == null || comprefaceUrl.isBlank() || detectionApiKey == null || detectionApiKey.isBlank()) {
            return -1;
        }
        String base64 = stripDataUrlPrefix(imageBase64);
        String url = comprefaceUrl.replaceAll("/$", "") + "/api/v1/detection/detect?det_prob_threshold=" + detProbThreshold;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", detectionApiKey);
            String body = objectMapper.writeValueAsString(java.util.Map.of("file", base64));
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode result = root.path("result");
                int count = result.isArray() ? result.size() : 0;
                log.debug("CompreFace [DETECTION] {} face(s) in frame", count);
                return count;
            }
        } catch (HttpStatusCodeException e) {
            log.debug("CompreFace detection HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString() != null ? e.getResponseBodyAsString().substring(0, Math.min(150, e.getResponseBodyAsString().length())) : "");
        } catch (Exception e) {
            log.warn("CompreFace detection error: {}", e.getMessage());
        }
        return -1;
    }

    private static String stripDataUrlPrefix(String base64) {
        if (base64 == null) return "";
        int i = base64.indexOf("base64,");
        return i >= 0 ? base64.substring(i + 7).trim() : base64.trim();
    }
}

