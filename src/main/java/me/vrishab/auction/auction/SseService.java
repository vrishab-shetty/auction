package me.vrishab.auction.auction;

import lombok.extern.slf4j.Slf4j;
import me.vrishab.auction.auction.dto.AuctionUpdateEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@Slf4j
@Service
public class SseService {

    private final Map<String, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    public SseService() {
        this.heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat, 30, 30, TimeUnit.SECONDS);
    }

    private void sendHeartbeat() {
        this.emitters.forEach((auctionId, auctionEmitters) -> {
            auctionEmitters.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("heartbeat")
                            .data("ping"));
                } catch (IOException e) {
                    removeEmitter(auctionId, emitter);
                }
            });
        });
    }

    public SseEmitter createEmitter(String auctionId) {
        // Set timeout to 5 minutes
        SseEmitter emitter = new SseEmitter(300_000L);

        this.emitters.computeIfAbsent(auctionId, k -> new CopyOnWriteArraySet<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(auctionId, emitter));
        emitter.onTimeout(() -> removeEmitter(auctionId, emitter));
        emitter.onError((e) -> removeEmitter(auctionId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data("Connected to auction " + auctionId));
        } catch (IOException e) {
            removeEmitter(auctionId, emitter);
        }

        return emitter;
    }

    public void broadcast(String auctionId, AuctionUpdateEvent event) {
        Set<SseEmitter> auctionEmitters = this.emitters.get(auctionId);
        if (auctionEmitters != null) {
            log.info("Broadcasting to {} listeners for auction {}", auctionEmitters.size(), auctionId);
            auctionEmitters.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("AUCTION_UPDATE")
                            .data(event));
                } catch (IOException e) {
                    removeEmitter(auctionId, emitter);
                }
            });
        }
    }

    private void removeEmitter(String auctionId, SseEmitter emitter) {
        Set<SseEmitter> auctionEmitters = this.emitters.get(auctionId);
        if (auctionEmitters != null) {
            auctionEmitters.remove(emitter);
            if (auctionEmitters.isEmpty()) {
                this.emitters.remove(auctionId);
            }
        }
    }
}
