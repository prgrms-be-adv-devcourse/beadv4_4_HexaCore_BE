package com.back.chat.adapter.in;

import com.back.chat.adapter.out.outbox.OutboxPublisher;
import com.back.chat.adapter.out.redis.RedisChatEventPublisher;
import com.back.chat.event.ChatMessageBlindedEvent;
import com.back.chat.event.ChatOutboxSavedEvent;
import com.back.chat.event.payload.ChatMessageBlindedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageBlindedListener {

    private final RedisChatEventPublisher redisChatMessagePublisher;

    private final OutboxPublisher outboxPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatMessageBlindedEvent event){
        try {
            ChatMessageBlindedPayload payload =
                    new ChatMessageBlindedPayload(
                            event.roomId(),
                            event.chatMessageId(),
                            LocalDateTime.now()
                    );

            redisChatMessagePublisher.publishMessageBlinded(event.roomId(), payload);
        } catch (Exception e) {
            log.warn("[REDIS][BLINDED] publish failed roomId={}, messageId={}, err={}",
                    event.roomId(), event.chatMessageId(), e.toString());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatOutboxSavedEvent event){
        outboxPublisher.publish(event);
    }
}
