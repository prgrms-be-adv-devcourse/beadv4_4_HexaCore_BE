package com.back.chat;

import com.back.chat.adapter.out.redis.RedisChatMessageSubscriber;
import com.back.chat.event.ChatEventEnvelope;
import com.back.chat.event.ChatEventType;
import com.back.chat.event.payload.ChatMessageDeletedPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisChatMessageSubscriberTest2 {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private ObjectMapper objectMapper;
    private RedisChatMessageSubscriber subscriber;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        subscriber = new RedisChatMessageSubscriber(objectMapper, messagingTemplate);
    }

    @Test
    void onMessage_MESSAGE_DELETED_shouldBroadcastToWebSocket() throws Exception {
        // given
        Long roomId = 1L;
        Long messageId = 100L;

        ChatMessageDeletedPayload payload =
                new ChatMessageDeletedPayload(roomId, messageId);

        ChatEventEnvelope envelope =
                new ChatEventEnvelope(ChatEventType.MESSAGE_DELETED,
                        objectMapper.valueToTree(payload));

        String json = objectMapper.writeValueAsString(envelope);

        Message redisMessage = new DefaultMessage(
                "chat-room".getBytes(StandardCharsets.UTF_8), // channel
                json.getBytes(StandardCharsets.UTF_8)         // body
        );

        // when
        subscriber.onMessage(redisMessage, null);

        // then
        verify(messagingTemplate).convertAndSend(
                "/topic/chat/room/" + roomId,
                payload
        );
    }
}

