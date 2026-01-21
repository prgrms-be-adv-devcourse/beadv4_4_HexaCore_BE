package com.back.chat;

import com.back.chat.adapter.out.redis.RedisChatEventPublisher;
import com.back.chat.event.payload.ChatMessagePayload;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisPublishIntegrationTest {

    @Autowired
    private RedisChatEventPublisher redisChatMessagePublisher;

    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Test
    void redis_publish_should_send_message_to_channel() throws Exception {
        // given
        Long roomId = 1L;
        String channel = "chatroom:" + roomId;

        ChatMessagePayload payload = new ChatMessagePayload(
                100L,
                roomId,
                1L,
                "redis publish test",
                false,
                LocalDateTime.now()
        );

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> receivedMessage = new AtomicReference<>();

        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                receivedMessage.set(new String(message.getBody()));
                latch.countDown();
            }
        };

        redisMessageListenerContainer.addMessageListener(listener, new ChannelTopic(channel));

        // when
        redisChatMessagePublisher.publishChatMessage(roomId, payload);

        // then
        boolean messageReceived = latch.await(2, TimeUnit.SECONDS);

        assertThat(messageReceived).isTrue();
        assertThat(receivedMessage.get()).contains("redis publish test");

        // cleanup
        redisMessageListenerContainer.removeMessageListener(listener);
    }
}
