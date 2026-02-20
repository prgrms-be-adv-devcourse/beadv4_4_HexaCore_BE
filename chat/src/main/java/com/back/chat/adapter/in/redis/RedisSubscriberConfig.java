package com.back.chat.adapter.in.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
@RequiredArgsConstructor
public class RedisSubscriberConfig {

    public static final String REDIS_PUBSUB_CHAT_ROOM_PATTERN = "chatroom:*";
    public static final String WS_CHAT_ROOM_TOPIC_PREFIX = "/topic/chat/room/";

    private final RedisConnectionFactory redisConnectionFactory;
    private final RedisChatMessageSubscriber redisChatMessageSubscriber;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);

        container.addMessageListener(
                messageListenerAdapter(),
                new PatternTopic(REDIS_PUBSUB_CHAT_ROOM_PATTERN)
        );

        return container;
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter() {
        return new MessageListenerAdapter(redisChatMessageSubscriber);
    }
}
