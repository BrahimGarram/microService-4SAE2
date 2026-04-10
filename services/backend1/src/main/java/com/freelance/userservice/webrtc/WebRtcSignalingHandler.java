package com.freelance.userservice.webrtc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebRTC signaling: forwards offer, answer, and ICE candidates between peers by user ID.
 */
@Component
public class WebRtcSignalingHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WebRtcSignalingHandler.class);
    private static final String ATTR_USER_ID = WebRtcHandshakeInterceptor.ATTR_USER_ID;

    /** userId -> WebSocketSession (one session per user for simplicity) */
    private final Map<Long, WebSocketSession> userIdToSession = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            close(session, "User ID missing");
            return;
        }
        WebSocketSession existing = userIdToSession.put(userId, session);
        if (existing != null && existing.isOpen()) {
            try {
                existing.close(CloseStatus.NORMAL);
            } catch (IOException e) {
                log.warn("Error closing previous session for user {}", userId, e);
            }
        }
        log.info("WebRTC signaling: user {} connected", userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long fromUserId = getUserId(session);
        if (fromUserId == null) {
            return;
        }
        String payload = message.getPayload();
        try {
            JsonNode root = objectMapper.readTree(payload);
            String type = root.has("type") ? root.get("type").asText() : null;
            if ("signal".equals(type)) {
                if (!root.has("toUserId")) {
                    sendError(session, "Missing toUserId");
                    return;
                }
                long toUserId = root.get("toUserId").asLong();
                JsonNode signal = root.get("signal");
                if (signal == null) {
                    sendError(session, "Missing signal");
                    return;
                }
                WebSocketSession toSession = userIdToSession.get(toUserId);
                if (toSession == null || !toSession.isOpen()) {
                    sendError(session, "User " + toUserId + " is not online");
                    return;
                }
                // Forward: send to peer with fromUserId so they know who sent it
                String forward = objectMapper.writeValueAsString(Map.of(
                        "type", "signal",
                        "fromUserId", fromUserId,
                        "signal", signal
                ));
                toSession.sendMessage(new TextMessage(forward));
            }
        } catch (Exception e) {
            log.warn("Invalid signaling message from user {}: {}", fromUserId, e.getMessage());
            sendError(session, "Invalid message: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserId(session);
        if (userId != null) {
            userIdToSession.remove(userId, session);
            log.info("WebRTC signaling: user {} disconnected", userId);
        }
    }

    private Long getUserId(WebSocketSession session) {
        Object attr = session.getAttributes().get(ATTR_USER_ID);
        if (attr instanceof Number) {
            return ((Number) attr).longValue();
        }
        return null;
    }

    private void sendError(WebSocketSession session, String message) {
        try {
            String json = objectMapper.writeValueAsString(Map.of("type", "error", "message", message));
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.warn("Failed to send error to session", e);
        }
    }

    private void close(WebSocketSession session, String reason) {
        try {
            session.close(CloseStatus.POLICY_VIOLATION.withReason(reason));
        } catch (IOException e) {
            log.warn("Error closing session", e);
        }
    }
}
