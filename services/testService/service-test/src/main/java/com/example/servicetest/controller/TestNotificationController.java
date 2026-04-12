package com.example.servicetest.controller;

import com.example.servicetest.dto.TestNotificationDTO;
import com.example.servicetest.messaging.TestNotificationProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-notifications")
public class TestNotificationController {

    private final TestNotificationProducer testNotificationProducer;

    public TestNotificationController(TestNotificationProducer testNotificationProducer) {
        this.testNotificationProducer = testNotificationProducer;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendTestNotification(@RequestBody TestNotificationDTO notification) {
        testNotificationProducer.sendTestNotification(notification);
        return ResponseEntity.ok("Test notification sent to RabbitMQ: Test ID " + notification.getTestId());
    }

    @PostMapping("/send-sample")
    public ResponseEntity<String> sendSampleNotification() {
        TestNotificationDTO sampleNotification = new TestNotificationDTO(
                1L,
                "Java Programming Test",
                "COMPLETED",
                100L,
                "John Doe"
        );
        
        testNotificationProducer.sendTestNotification(sampleNotification);
        return ResponseEntity.ok("Sample test notification sent to RabbitMQ");
    }
}
