package me.vrishab.auction.system.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.vrishab.auction.auction.NotificationService;
import me.vrishab.auction.auction.dto.BidUpdateMessage;
import org.springframework.stereotype.Component;

@Component
public class NotificationMessageListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationMessageListener(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    public void handleMessage(String message) {
        try {
            BidUpdateMessage updateMessage = objectMapper.readValue(message, BidUpdateMessage.class);
            notificationService.broadcast(updateMessage.auctionId().toString(), message);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to parse incoming pub/sub message: " + e.getMessage());
        }
    }
}
