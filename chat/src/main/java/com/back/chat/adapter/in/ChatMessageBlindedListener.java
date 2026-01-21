package com.back.chat.adapter.in;

import com.back.chat.adapter.out.redis.RedisChatEventPublisher;
import com.back.chat.event.ChatMessageBlindedEvent;
import com.back.chat.event.payload.ChatMessageBlindedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatMessageBlindedListener {

    private final RedisChatEventPublisher redisChatMessagePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatMessageBlindedEvent event){

        // TODO: 유저모듈 연동해서 blindCount +1 증가 로직 구현.

        // 실시간 블라인드 전파
        ChatMessageBlindedPayload payload = new ChatMessageBlindedPayload(event.roomId(), event.chatMessageId(), LocalDateTime.now());
        redisChatMessagePublisher.publishMessageBlinded(event.roomId(), payload);
    }
}
