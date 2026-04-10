package com.freelance.userservice.config;

import com.freelance.userservice.webrtc.WebRtcHandshakeInterceptor;
import com.freelance.userservice.webrtc.WebRtcSignalingHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for WebRTC signaling.
 * Registers the signaling endpoint at /ws/signal.
 * Client connects with: ws://host:8081/ws/signal?token=Bearer_xxx
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebRtcHandshakeInterceptor handshakeInterceptor;
    private final WebRtcSignalingHandler signalingHandler;

    public WebSocketConfig(WebRtcHandshakeInterceptor handshakeInterceptor, WebRtcSignalingHandler signalingHandler) {
        this.handshakeInterceptor = handshakeInterceptor;
        this.signalingHandler = signalingHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(signalingHandler, "/ws/signal")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins("http://localhost:4200");
    }
}
