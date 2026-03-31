package me.vrishab.auction.system.configuration;

import me.vrishab.auction.auction.NotificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisPubSubConfig {

    public static final String BID_UPDATES_CHANNEL = "bid-updates";

    @Bean
    RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory,
                                                 MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new ChannelTopic(BID_UPDATES_CHANNEL));
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(NotificationMessageListener receiver) {
        // "onMessage" is the default method for MessageListenerAdapter if implementing MessageListener,
        // but since we are specifying the delegate object, we tell the adapter which method to call.
        return new MessageListenerAdapter(receiver, "handleMessage");
    }
}
