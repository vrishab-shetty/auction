package me.vrishab.auction.auction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
    }

    @AfterEach
    void tearDown() {
        notificationService.shutdown();
    }

    @Test
    void testCreateEmitter() {
        SseEmitter emitter = notificationService.createEmitter("auction1");
        assertNotNull(emitter);
    }

    @Test
    void testBroadcast() {
        SseEmitter emitter1 = notificationService.createEmitter("auction1");
        SseEmitter emitter2 = notificationService.createEmitter("auction1");

        // This will send successfully if emitters are connected, in this case they just hold the message
        assertDoesNotThrow(() -> notificationService.broadcast("auction1", "{\"message\":\"test\"}"));
    }
}
