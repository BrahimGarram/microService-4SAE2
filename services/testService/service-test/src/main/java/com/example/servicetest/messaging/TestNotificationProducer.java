package com.example.servicetest.messaging;

import com.example.servicetest.config.RabbitMQConfig;
import com.example.servicetest.dto.TestNotificationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class TestNotificationProducer {

    private final RabbitTemplate rabbitTemplate;
    private static final Logger log = LoggerFactory.getLogger(TestNotificationProducer.class);

    public TestNotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendTestNotification(TestNotificationDTO notification) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.TEST_NOTIFICATION_QUEUE, notification);
            log.info("Test notification envoyée à la queue : {} - Test ID: {}", 
                    RabbitMQConfig.TEST_NOTIFICATION_QUEUE, notification.getTestId());
        } catch (AmqpException e) {
            log.error("Erreur lors de l'envoi de la notification à RabbitMQ", e);
            throw e;
        }
    }
}
