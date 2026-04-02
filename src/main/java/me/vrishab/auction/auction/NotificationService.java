package me.vrishab.auction.auction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.vrishab.auction.auction.dto.AuctionUpdateEvent;
import me.vrishab.auction.auction.dto.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public NotificationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SseEmitter createEmitter(UUID auctionId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Infinite timeout for bid updates

        this.emitters.computeIfAbsent(auctionId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(auctionId, emitter));
        emitter.onTimeout(() -> removeEmitter(auctionId, emitter));
        emitter.onError((e) -> removeEmitter(auctionId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data(new NotificationEvent<>("INIT", "Connected to auction feed")));
        } catch (IOException e) {
            removeEmitter(auctionId, emitter);
        }

        return emitter;
    }

    private void removeEmitter(UUID auctionId, SseEmitter emitter) {
        List<SseEmitter> auctionEmitters = this.emitters.get(auctionId);
        if (auctionEmitters != null) {
            auctionEmitters.remove(emitter);
            if (auctionEmitters.isEmpty()) {
                this.emitters.remove(auctionId);
            }
        }
    }

    public void handleMessage(String message) {
        try {
            AuctionUpdateEvent event = this.objectMapper.readValue(message, AuctionUpdateEvent.class);
            broadcast(event);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse Redis message: {}", message, e);
        }
    }

    private void broadcast(AuctionUpdateEvent event) {
        List<SseEmitter> auctionEmitters = this.emitters.get(event.auctionId());
        if (auctionEmitters != null) {
            NotificationEvent<AuctionUpdateEvent> notification = new NotificationEvent<>("BID_UPDATE", event);
            for (SseEmitter emitter : auctionEmitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("BID_UPDATE")
                            .id(UUID.randomUUID().toString())
                            .data(notification));
                } catch (IOException e) {
                    removeEmitter(event.auctionId(), emitter);
                }
            }
        }
    }

    @Scheduled(fixedRate = 30000) // 30 seconds heartbeat
    public void sendHeartbeat() {
        NotificationEvent<String> heartbeat = new NotificationEvent<>("HEARTBEAT", "ping");
        this.emitters.forEach((auctionId, emittersList) -> {
            emittersList.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("HEARTBEAT")
                            .data(heartbeat));
                } catch (IOException e) {
                    removeEmitter(auctionId, emitter);
                }
            });
        });
    }
}
