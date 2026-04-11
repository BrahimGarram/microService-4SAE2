package com.freelance.userservice.webrtc;

import com.freelance.userservice.repository.UserRepository;
import com.freelance.userservice.util.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Interceptor that validates JWT from query param and puts internal userId in handshake attributes.
 * Client must connect with: ws://host/ws/signal?token=Bearer_xxx or token=xxx
 */
@Component
public class WebRtcHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_USER_ID = "userId";

    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;

    public WebRtcHandshakeInterceptor(JwtDecoder jwtDecoder, UserRepository userRepository) {
        this.jwtDecoder = jwtDecoder;
        this.userRepository = userRepository;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest)) {
            return false;
        }
        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
        String query = servletRequest.getServletRequest().getQueryString();
        if (query == null || !query.contains("token=")) {
            return false;
        }
        String token = null;
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                token = param.substring("token=".length());
                break;
            }
        }
        if (token == null || token.isBlank()) {
            return false;
        }
        if (token.startsWith("Bearer%20")) {
            token = token.substring("Bearer%20".length());
        } else if (token.startsWith("Bearer ")) {
            token = token.substring("Bearer ".length());
        }
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String keycloakId = JwtUtil.extractKeycloakId(jwt);
            Long userId = userRepository.findByKeycloakId(keycloakId)
                    .map(u -> u.getId())
                    .orElse(null);
            if (userId == null) {
                return false;
            }
            attributes.put(ATTR_USER_ID, userId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
