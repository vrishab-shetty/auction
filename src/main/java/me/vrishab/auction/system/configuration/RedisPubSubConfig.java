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
    public ChannelTopic bidUpdatesTopic() {
        return new ChannelTopic(BID_UPDATES_CHANNEL);
    }

    @Bean
    public MessageListenerAdapter messageListener(NotificationService notificationService) {
        return new MessageListenerAdapter(notificationService, "handleMessage");
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory,
                                                       MessageListenerAdapter messageListener,
                                                       ChannelTopic bidUpdatesTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(messageListener, bidUpdatesTopic);
        return container;
    }
}
