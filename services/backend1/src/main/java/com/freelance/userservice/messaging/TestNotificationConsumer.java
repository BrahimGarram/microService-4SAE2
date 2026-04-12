package com.freelance.userservice.messaging;

import com.freelance.userservice.config.RabbitMQConfig;
import com.freelance.userservice.dto.TestNotificationDTO;
import com.freelance.userservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class TestNotificationConsumer {

    private final NotificationService notificationService;
    private static final Logger log = LoggerFactory.getLogger(TestNotificationConsumer.class);

    public TestNotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Écoute la file TEST_NOTIFICATION_QUEUE
    // Spring convertit automatiquement le JSON en TestNotificationDTO grâce au MessageConverter
    @RabbitListener(queues = RabbitMQConfig.TEST_NOTIFICATION_QUEUE, 
                    containerFactory = "rabbitListenerContainerFactory")
    public void receiveTestNotification(TestNotificationDTO notification) {
        log.info("TestNotificationDTO reçu depuis RabbitMQ : {}", notification);
        
        // Déléguer la logique métier
        notificationService.processTestNotification(notification);
    }
}
