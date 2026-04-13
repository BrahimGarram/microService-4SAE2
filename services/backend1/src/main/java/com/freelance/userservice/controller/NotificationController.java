package com.freelance.userservice.controller;

import com.freelance.userservice.dto.CreateNotificationRequest;
import com.freelance.userservice.dto.NotificationDTO;
import com.freelance.userservice.dto.UserDTO;
import com.freelance.userservice.service.NotificationService;
import com.freelance.userservice.service.UserService;
import com.freelance.userservice.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for in-app notifications (e.g. new proposal, proposal accepted).
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<NotificationDTO> create(
            @Valid @RequestBody CreateNotificationRequest request,
            Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = JwtUtil.extractKeycloakId(jwt);
        UserDTO currentUser = userService.getCurrentUser(keycloakId);
        NotificationDTO created = notificationService.create(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = JwtUtil.extractKeycloakId(jwt);
        UserDTO currentUser = userService.getCurrentUser(keycloakId);
        return ResponseEntity.ok(notificationService.getByTargetUser(currentUser.getId()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = JwtUtil.extractKeycloakId(jwt);
        UserDTO currentUser = userService.getCurrentUser(keycloakId);
        long count = notificationService.getUnreadCount(currentUser.getId());
        Map<String, Long> body = new HashMap<>();
        body.put("count", count);
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = JwtUtil.extractKeycloakId(jwt);
        UserDTO currentUser = userService.getCurrentUser(keycloakId);
        return ResponseEntity.ok(notificationService.markAsRead(id, currentUser.getId()));
    }
}
