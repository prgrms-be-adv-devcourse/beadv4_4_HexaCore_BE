package com.back.chat.app.listener;

import com.back.chat.adapter.out.redis.RedisChatEventPublisher;
import com.back.chat.domain.event.ChatMessageDeletedEvent;
import com.back.chat.adapter.out.redis.payload.ChatMessageDeletedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageDeletedListener {
    private final RedisChatEventPublisher redisChatEventPublisher;

    @Async("chatRedisExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatMessageDeletedEvent event){
        ChatMessageDeletedPayload payload = new ChatMessageDeletedPayload(event.roomId(), event.messageId());
        redisChatEventPublisher.publishMessageDeleted(event.roomId(), payload);
    }
}
