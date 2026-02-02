package com.back.chat;

import com.back.chat.adapter.out.redis.RedisChatMessageSubscriber;
import com.back.chat.domain.MessageStatus;
import com.back.chat.event.ChatEventEnvelope;
import com.back.chat.event.ChatEventType;
import com.back.chat.event.payload.ChatMessagePayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RedisChatMessageSubscriberTest {

    private ObjectMapper objectMapper;
    private SimpMessagingTemplate messagingTemplate;
    private RedisChatMessageSubscriber subscriber;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        // subscriber 내부에서 LocalDateTime을 역직렬화할 수 있게
        // (너 프로젝트에서는 JacksonConfig 빈이 담당하지만 테스트는 여기서 최소로)
        messagingTemplate = mock(SimpMessagingTemplate.class);
        subscriber = new RedisChatMessageSubscriber(objectMapper, messagingTemplate);
    }

    @Test
    void onMessage_shouldBroadcastToStompTopic_whenChatMessageEventArrives() throws Exception {
        // given
        long roomId = 3L;

        ChatMessagePayload payload = new ChatMessagePayload(
                101L,        // messageId
                2L,          // userId
                roomId,      // roomId
                "hello",     // content
                MessageStatus.NORMAL,       // isBlinded
                LocalDateTime.of(2026, 1, 30, 14, 50, 0) // createdAt
        );

        ChatEventEnvelope envelope = new ChatEventEnvelope(
                ChatEventType.CHAT_MESSAGE,
                objectMapper.valueToTree(payload)
        );

        String json = objectMapper.writeValueAsString(envelope);

        byte[] channelBytes = ("chatroom:" + roomId).getBytes(StandardCharsets.UTF_8);
        byte[] bodyBytes = json.getBytes(StandardCharsets.UTF_8);

        Message redisMessage = new DefaultMessage(channelBytes, bodyBytes);

        // when
        subscriber.onMessage(redisMessage, null);

        // then
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/chat/room/" + roomId), payloadCaptor.capture());

        assertThat(payloadCaptor.getValue())
                .isInstanceOf(ChatMessagePayload.class);

        ChatMessagePayload sent = (ChatMessagePayload) payloadCaptor.getValue();
        assertThat(sent.roomId()).isEqualTo(roomId);
        assertThat(sent.messageId()).isEqualTo(101L);
        assertThat(sent.content()).isEqualTo("hello");
    }
}

