package com.back.user.adapter.in.chat;

import com.back.common.chat.ChatMessageBlindedKafkaEvent;
import com.back.common.event.Envelope;
import com.back.user.app.UserFacade;
import com.back.user.kafka.UserConsumedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageBlindedEventListener {

    private static final String EVENT_TYPE = "MESSAGE_BLINDED";

    private final UserConsumedEventRepository userConsumedEventRepository;
    private final UserFacade userFacade;
    private final JsonMapper jsonMapper;

    @KafkaListener(
            topics = "${custom.kafka.topic.chat-blind-requested}",
            containerFactory = "stringKafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(
            String json,
            Acknowledgment ack,
            ConsumerRecord<String, String> record
    ) {

        LocalDateTime now = LocalDateTime.now();

        Envelope<ChatMessageBlindedKafkaEvent> envelope;

        try {
            envelope = jsonMapper.readValue(
                            json,
                            new TypeReference<Envelope<ChatMessageBlindedKafkaEvent>>() {}
                    );
        } catch (Exception e) {
            throw new IllegalArgumentException("블라인드 요청 역직렬화 실패", e);
        }

        String eventId = envelope.header().eventId();
        ChatMessageBlindedKafkaEvent event = envelope.payload();

        // 1) 멱등 게이트 (insert 시도)
        int updated = userConsumedEventRepository.insertIfAbsent(
                UUID.fromString(eventId),
                EVENT_TYPE,
                now
        );

        if (updated == 0) {
            log.info("[USER][KAFKA] DUPLICATE ignore eventId={}, userId={}, topic={}, partition={}, offset={}",
                    eventId, event.authorUserId(), record.topic(), record.partition(), record.offset());
            ack.acknowledge();
            return;
        }

        // 2) 비즈니스 처리
        userFacade.incrementBlindCount(event.authorUserId(), now);

        TxAfterCommit.run(() -> {
            ack.acknowledge();
            log.info("[USER][KAFKA] SUCCESS eventId={}, userId={}, topic={}, partition={}, offset={}",
                    eventId, event.authorUserId(), record.topic(), record.partition(), record.offset());
        });
    }
}
