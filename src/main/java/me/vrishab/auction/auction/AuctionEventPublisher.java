package me.vrishab.auction.auction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.vrishab.auction.auction.dto.AuctionUpdateEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionEventPublisher {

    private final RedisTemplate<String, Object> eventRedisTemplate;
    private final ChannelTopic topic;

    public void publish(AuctionUpdateEvent event) {
        log.info("Publishing auction update event to Redis: {}", event);
        eventRedisTemplate.convertAndSend(topic.getTopic(), event);
    }
}
