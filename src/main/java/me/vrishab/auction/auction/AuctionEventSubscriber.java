package me.vrishab.auction.auction;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.vrishab.auction.auction.dto.AuctionUpdateEvent;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionEventSubscriber implements MessageListener {

    private final SseService sseService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            AuctionUpdateEvent event = objectMapper.readValue(message.getBody(), AuctionUpdateEvent.class);
            log.debug("Received auction event from Redis: {}", event);
            sseService.broadcast(event.auctionId(), event);
        } catch (Exception e) {
            log.error("Failed to deserialize auction update event from Redis", e);
        }
    }
}
