package com.back.chat.adapter.in.kafka;

import com.back.chat.adapter.in.kafka.payload.BrandDeletedPayload;
import com.back.chat.adapter.out.idempotency.IdempotencyService;
import com.back.chat.app.ChatFacade;
import com.back.common.event.Envelope;
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

import static com.back.chat.adapter.in.kafka.KafkaListenerUtil.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaBrandDeletedListener {

    private final JsonMapper jsonMapper;
    private final ChatFacade chatFacade;

    private final IdempotencyService idempotencyService;

    @KafkaListener(
            topics = "${custom.kafka.topic.product-brand-deleted:product.brand.deleted}",
            containerFactory = "stringKafkaListenerContainerFactory"
    )
    @Transactional
    public void handle(
            String json,
            Acknowledgment ack,
            ConsumerRecord<String, String> record
    ) {
        Envelope<BrandDeletedPayload> envelope;

        try {
            envelope = jsonMapper.readValue(
                    json,
                    new TypeReference<Envelope<BrandDeletedPayload>>() {}
            );
        } catch (Exception e) {
            log.error(
                    "[CHAT][KAFKA][DESERIALIZE_FAIL] payload={}, partition={}, offset={}",
                    json, record.partition(), record.offset(), e
            );
            throw new IllegalArgumentException("브랜드 삭제 이벤트 역직렬화 실패", e);
        }

        String eventIdSafe = safeEventId(envelope);
        UUID eventId = safeUuid(eventIdSafe);

        if (eventId == null) {
            log.error(
                    "[CHAT][KAFKA] eventId missing/invalid. skip. eventIdRaw={}, partition={}, offset={}",
                    eventIdSafe, record.partition(), record.offset()
            );
            ack.acknowledge();
            return;
        }

        String eventType = (envelope.header() == null) ? null : envelope.header().eventType();
        if (eventType == null || eventType.isBlank()) {
            log.error(
                    "[CHAT][KAFKA] eventType missing. skip. eventId={}, partition={}, offset={}",
                    eventId, record.partition(), record.offset()
            );
            ack.acknowledge();
            return;
        }

        BrandDeletedPayload payload = envelope.payload();
        Long brandId = (payload == null) ? null : payload.brandId();

        if (brandId == null) {
            log.warn(
                    "[CHAT][KAFKA] invalid/empty brandId. eventId={}, partition={}, offset={}",
                    eventId, record.partition(), record.offset()
            );

            ack.acknowledge();
            return;
        }

        // 멱등 게이트.
        LocalDateTime now = LocalDateTime.now();
        boolean acquired = idempotencyService.tryAcquire(eventId, eventType, now);
        if (!acquired) {
            log.info(
                    "[CHAT][KAFKA] DUPLICATE ignore. eventId={}, eventType={}, brandId={}, partition={}, offset={}",
                    eventId, eventType, brandId, record.partition(), record.offset()
            );
            ack.acknowledge();
            return;
        }

        log.info(
                "[CHAT][KAFKA] BRAND_DELETED received. eventId={}, brandId={}, partition={}, offset={}",
                eventId, brandId, record.partition(), record.offset()
        );

        try {
            int updated = chatFacade.deleteChatRoom(brandId);

            if (updated == 1) {
                log.info(
                        "[CHAT][KAFKA] chat room deleted. eventId={}, brandId={}, partition={}, offset={}",
                        eventId, brandId, record.partition(), record.offset()
                );
            } else {
                log.info(
                        "[CHAT][KAFKA] chat room already deleted (idempotent). eventId={}, brandId={}, partition={}, offset={}",
                        eventId, brandId, record.partition(), record.offset()
                );
            }

            ackAfterCommit(ack);

        } catch (Exception e) {
            log.error(
                    "[CHAT][KAFKA] chat room deletion failed. eventId={}, brandId={}, partition={}, offset={}",
                    eventId, brandId, record.partition(), record.offset(), e
            );
            throw e;
        }
    }
}
