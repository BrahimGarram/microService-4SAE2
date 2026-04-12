package com.freelance.userservice.service;

import com.freelance.userservice.dto.TestNotificationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    
    // Simulons un cache en mémoire pour les notifications reçues
    private final List<TestNotificationDTO> receivedNotifications = new ArrayList<>();

    public void processTestNotification(TestNotificationDTO notification) {
        log.info("Traitement de la notification de test : {}", notification.getTestTitle());
        
        // Ajouter à la liste des notifications
        addToNotifications(notification);
        
        // Envoyer une notification à l'utilisateur (simulation)
        sendNotificationToUser(notification);
    }

    private void addToNotifications(TestNotificationDTO notification) {
        receivedNotifications.add(notification);
        log.info("Notification ajoutée : Test '{}' - Status: {}", 
                notification.getTestTitle(), notification.getStatus());
    }

    private void sendNotificationToUser(TestNotificationDTO notification) {
        // Simulation de l'envoi d'une notification (email, push, etc.)
        log.info("Notification envoyée à l'utilisateur {} : Test '{}' - Status: {}", 
                notification.getUserName(), notification.getTestTitle(), notification.getStatus());
    }

    // Récupérer toutes les notifications reçues
    public List<TestNotificationDTO> getAllNotifications() {
        return new ArrayList<>(receivedNotifications);
    }

    // Récupérer les notifications pour un utilisateur spécifique
    public List<TestNotificationDTO> getNotificationsByUserId(Long userId) {
        return receivedNotifications.stream()
                .filter(n -> n.getUserId().equals(userId))
                .toList();
    }
}
