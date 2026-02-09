package com.back.user.adapter.in.chat;

import com.back.common.chat.ChatMessageBlindedKafkaEvent;
import com.back.user.app.UserFacade;
import com.back.user.kafka.UserIdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageBlindedEventListener {

    private static final String EVENT_TYPE = "MESSAGE_BLINDED";

    private final UserIdempotencyService idempotencyService;
    private final UserFacade userFacade;

    @KafkaListener(
            topics = "${custom.kafka.topic.chat-blind-requested}",
            containerFactory = "chatMessageBlindedKafkaListenerContainerFactory"
    )
    public void consume(ChatMessageBlindedKafkaEvent event,
                        Acknowledgment ack,
                        ConsumerRecord<String, ChatMessageBlindedKafkaEvent> record) {
        LocalDateTime now = LocalDateTime.now();

        try {
            // 1) 멱등 게이트 (insert 시도)
            try {
                idempotencyService.insertConsumed(UUID.fromString(event.eventId()), EVENT_TYPE, now);
            } catch (DataIntegrityViolationException dup) {
                log.info("[USER][KAFKA] DUPLICATE ignore eventId={}, userId={}, topic={}, partition={}, offset={}",
                        event.eventId(), event.authorUserId(), record.topic(), record.partition(), record.offset());
                ack.acknowledge();
                return;
            }

            // 2) 비즈니스 처리
            userFacade.incrementBlindCount(event.authorUserId(), now);

            ack.acknowledge();

            log.info("[USER][KAFKA] SUCCESS eventId={}, userId={}, topic={}, partition={}, offset={}",
                    event.eventId(), event.authorUserId(), record.topic(), record.partition(), record.offset());
        } catch (Exception e) {
            throw e;
        }
    }
}
