package com.back.chat.adapter.in.kafka;

import com.back.chat.adapter.out.idempotency.IdempotencyService;
import com.back.chat.app.ChatFacade;
import com.back.chat.adapter.in.kafka.payload.BrandCreatedPayload;
import com.back.chat.domain.event.ChatEventType;
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
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.back.chat.adapter.in.kafka.KafkaListenerUtil.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaBrandCreatedListener {

    private final JsonMapper jsonMapper;
    private final ChatFacade chatFacade;

    private final IdempotencyService idempotencyService;

    @KafkaListener(
            topics = "${custom.kafka.topic.product-brand-created:product.brand.created}",
            containerFactory = "stringKafkaListenerContainerFactory"
    )
    @Transactional
    public void handle(String json, Acknowledgment ack, ConsumerRecord<String, String> record) {
        final Envelope<BrandCreatedPayload> envelope;

        try {
            envelope = jsonMapper.readValue(json, new TypeReference<Envelope<BrandCreatedPayload>>() {});
        } catch (Exception e) {
            log.error("[CHAT][KAFKA][DESERIALIZE_FAIL] payload={}, partition={}, offset={}",
                    json, record.partition(), record.offset(), e);
            throw new IllegalArgumentException("브랜드 생성 이벤트 역직렬화 실패", e);
        }

        String eventIdRaw = safeEventId(envelope);
        UUID eventId = safeUuid(eventIdRaw);

        if (eventId == null) {
            log.error("[CHAT][KAFKA] eventId missing/invalid. skip. eventIdRaw={}, partition={}, offset={}",
                    eventIdRaw, record.partition(), record.offset());
            ack.acknowledge();
            return;
        }

        String eventType = (envelope.header() == null) ? null : envelope.header().eventType();
        if (eventType == null || eventType.isBlank()) {
            log.error("[CHAT][KAFKA] eventType missing. skip. eventId={}, partition={}, offset={}",
                    eventId, record.partition(), record.offset());
            ack.acknowledge();
            return;
        }

        BrandCreatedPayload payload = envelope.payload();
        List<BrandCreatedPayload.BrandIdOnly> brands = (payload == null) ? null : payload.getBrands();

        if (brands == null || brands.isEmpty()) {
            log.warn("[CHAT][KAFKA] invalid/empty brands. eventId={}, partition={}, offset={}",
                    eventId, record.partition(), record.offset());
            ack.acknowledge();
            return;
        }

        List<Long> brandIds = brands.stream()
                .map(BrandCreatedPayload.BrandIdOnly::getBrandId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (brandIds.isEmpty()) {
            log.warn("[CHAT][KAFKA] brandIds empty after normalize. eventId={}, partition={}, offset={}",
                    eventId, record.partition(), record.offset());
            ack.acknowledge();
            return;
        }

        // 멱등 게이트.
        LocalDateTime now = LocalDateTime.now();
        boolean acquired = idempotencyService.tryAcquire(eventId, eventType, now);
        if (!acquired) {
            log.info("[CHAT][KAFKA] DUPLICATE ignore. eventId={}, eventType={}, partition={}, offset={}",
                    eventId, eventType, record.partition(), record.offset());
            ack.acknowledge();
            return;
        }

        try {
            chatFacade.createChatRoomsIfAbsent(brandIds);
            ackAfterCommit(ack);
        } catch (Exception e) {
            log.error("[CHAT][KAFKA] chat room creation failed. eventId={}, brandIds(size={}), partition={}, offset={}",
                    eventId, brandIds.size(), record.partition(), record.offset(), e);
            throw e;
        }
    }
    }

