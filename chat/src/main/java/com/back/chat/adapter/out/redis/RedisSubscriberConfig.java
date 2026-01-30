package com.back.chat.adapter.out.redis;

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

    private static final String CHAT_ROOM_CHANNEL_PATTERN = "chatroom:*";

    private final RedisConnectionFactory redisConnectionFactory;
    private final RedisChatMessageSubscriber redisChatMessageSubscriber;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);

        container.addMessageListener(
                messageListenerAdapter(),
                new PatternTopic(CHAT_ROOM_CHANNEL_PATTERN)
        );

        return container;
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter() {
        /*
         * RedisChatMessageSubscriber가 MessageListener를 직접 구현했기 때문에
         * 별도 메서드 지정 없이 그대로 감싸기만 하면 됨
         */
        return new MessageListenerAdapter(redisChatMessageSubscriber);
    }
}
