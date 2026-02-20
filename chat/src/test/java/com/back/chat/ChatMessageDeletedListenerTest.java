package com.back.chat;

import com.back.chat.app.listener.ChatMessageDeletedListener;
import com.back.chat.adapter.out.redis.RedisChatEventPublisher;
import com.back.chat.domain.event.ChatMessageDeletedEvent;
import com.back.chat.adapter.out.redis.payload.ChatMessageDeletedPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ChatMessageDeletedListenerTest {

    @Mock
    private RedisChatEventPublisher redisChatEventPublisher;

    @InjectMocks
    private ChatMessageDeletedListener listener;

    @Test
    @DisplayName("메시지 삭제 이벤트 발생 -> Redis로 삭제 이벤트를 발행")
    void handle_shouldPublishMessageDeletedEvent() {
        // given
        Long roomId = 1L;
        Long messageId = 100L;

        ChatMessageDeletedEvent event = new ChatMessageDeletedEvent(messageId, roomId);

        // when
        listener.handle(event);

        // then
        ArgumentCaptor<ChatMessageDeletedPayload> captor =
                ArgumentCaptor.forClass(ChatMessageDeletedPayload.class);

        verify(redisChatEventPublisher).publishMessageDeleted(eq(roomId), captor.capture());

        ChatMessageDeletedPayload payload = captor.getValue();
        assertThat(payload.roomId()).isEqualTo(roomId);
        assertThat(payload.messageId()).isEqualTo(messageId);
    }
}

