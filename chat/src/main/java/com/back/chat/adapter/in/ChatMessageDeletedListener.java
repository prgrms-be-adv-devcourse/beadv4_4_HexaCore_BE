package com.back.chat.adapter.in;

import com.back.chat.adapter.out.redis.RedisChatEventPublisher;
import com.back.chat.event.ChatMessageBlindedEvent;
import com.back.chat.event.ChatMessageDeletedEvent;
import com.back.chat.event.payload.ChatMessageBlindedPayload;
import com.back.chat.event.payload.ChatMessageDeletedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageDeletedListener {
    private final RedisChatEventPublisher redisChatEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatMessageDeletedEvent event){
        ChatMessageDeletedPayload payload = new ChatMessageDeletedPayload(event.roomId(), event.messageId());
        redisChatEventPublisher.publishMessageDeleted(event.roomId(), payload);
    }
}
