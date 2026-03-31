package me.vrishab.auction.auction;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationService {

    private final ConcurrentHashMap<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    public NotificationService() {
        // Start a heartbeat to keep connections alive and detect disconnected clients
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat, 15, 15, TimeUnit.SECONDS);
    }

    public SseEmitter createEmitter(String auctionId) {
        // Create an emitter with a reasonable timeout, e.g., 30 minutes. SseEmitter timeout is in ms.
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        List<SseEmitter> auctionEmitters = emitters.computeIfAbsent(auctionId, k -> new CopyOnWriteArrayList<>());
        auctionEmitters.add(emitter);

        Runnable removeEmitter = () -> {
            auctionEmitters.remove(emitter);
            if (auctionEmitters.isEmpty()) {
                emitters.remove(auctionId);
            }
        };

        emitter.onCompletion(removeEmitter);
        emitter.onTimeout(removeEmitter);
        emitter.onError(e -> removeEmitter.run());

        // Send an initial connected event
        try {
            emitter.send(SseEmitter.event().name("connected").data("Successfully connected to auction stream."));
        } catch (IOException e) {
            removeEmitter.run();
        }

        return emitter;
    }

    public void broadcast(String auctionId, String eventData) {
        List<SseEmitter> auctionEmitters = emitters.get(auctionId);
        if (auctionEmitters != null) {
            for (SseEmitter emitter : auctionEmitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("bid-update")
                            .data(eventData));
                } catch (IOException e) {
                    emitter.completeWithError(e); // This will trigger the onError callback and remove the emitter
                }
            }
        }
    }

    private void sendHeartbeat() {
        emitters.forEach((auctionId, auctionEmitters) -> {
            for (SseEmitter emitter : auctionEmitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("heartbeat")
                            .data("ping"));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }
        });
    }

    public void shutdown() {
        heartbeatExecutor.shutdownNow();
    }
}
