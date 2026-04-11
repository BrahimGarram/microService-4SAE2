package com.example.servicetest.api;

/**
 * Response from the face verification API (CompreFace).
 */
public record FaceVerifyResponse(
        boolean match,
        double similarity,
        String message,
        Boolean noFaceDetected,
        Boolean multipleFacesDetected
) {

    public static FaceVerifyResponse ok(boolean match, double similarity) {
        return new FaceVerifyResponse(match, similarity, null, null, null);
    }

    /** No face in the frame (detection). */
    public static FaceVerifyResponse noFace() {
        return new FaceVerifyResponse(false, 0.0, null, true, null);
    }

    /** More than one face in the frame (detection). */
    public static FaceVerifyResponse multipleFaces() {
        return new FaceVerifyResponse(false, 0.0, null, null, true);
    }

    public static FaceVerifyResponse error(String message) {
        return new FaceVerifyResponse(false, 0.0, message, null, null);
    }
}

