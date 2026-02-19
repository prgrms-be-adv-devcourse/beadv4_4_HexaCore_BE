package com.back.chat.adapter.in.kafka;

import com.back.chat.app.ChatFacade;
import com.back.chat.adapter.in.kafka.payload.BrandCreatedPayload;
import com.back.common.event.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaBrandCreatedListener {

    private final JsonMapper jsonMapper;
    private final ChatFacade chatFacade;

    @KafkaListener(
            topics = "${custom.kafka.topic.product-brand-created:product.brand.created}",
            containerFactory = "stringKafkaListenerContainerFactory"
    )
    @Transactional
    public void handle(
            String json,
            Acknowledgment ack,
            ConsumerRecord<String, String> record
    ) {
        Envelope<BrandCreatedPayload> envelope;

        try {
            envelope = jsonMapper.readValue(
                    json,
                    new TypeReference<Envelope<BrandCreatedPayload>>() {}
            );
        } catch (Exception e) {
            log.error(
                    "[CHAT][KAFKA][DESERIALIZE_FAIL] payload={}, partition={}, offset={}",
                    json, record.partition(), record.offset(), e
            );
            // not-retryable로 설정되어 있으니: DB 로깅(Recoverer) 후 스킵
            throw new IllegalArgumentException("브랜드 생성 이벤트 역직렬화 실패", e);
        }

        String eventIdSafe = safeEventId(envelope);

        BrandCreatedPayload payload = (envelope == null) ? null : envelope.payload();
        List<BrandCreatedPayload.BrandIdOnly> brandIdOnlyList =
                (payload == null) ? null : payload.getBrands();

        if (brandIdOnlyList == null || brandIdOnlyList.isEmpty()) {
            log.warn(
                    "[CHAT][KAFKA] invalid/empty brands. eventId={}, partition={}, offset={}",
                    eventIdSafe, record.partition(), record.offset()
            );
            ack.acknowledge();
            return;
        }

        List<Long> brandIds =
                brandIdOnlyList.stream()
                        .map(BrandCreatedPayload.BrandIdOnly::getBrandId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        if (brandIds.isEmpty()) {
            log.warn(
                    "[CHAT][KAFKA] brandIds empty after normalize. eventId={}, partition={}, offset={}",
                    eventIdSafe, record.partition(), record.offset()
            );
            ack.acknowledge();
            return;
        }

        log.info(
                "[CHAT][KAFKA] BRAND_CREATED received. eventId={}, brandIds(size={}), partition={}, offset={}",
                eventIdSafe, brandIds.size(), record.partition(), record.offset()
        );

        try {
            chatFacade.createChatRoomsIfAbsent(brandIds);

            // 트랜잭션 커밋 이후 ack
            ackAfterCommit(ack);

        } catch (Exception e) {
            log.error(
                    "[CHAT][KAFKA] chat room creation failed. eventId={}, brandIds(size={}), partition={}, offset={}",
                    eventIdSafe, brandIds.size(), record.partition(), record.offset(), e
            );
            throw e; // ErrorHandler가 retry/로그/스킵을 처리
        }
    }

    private static String safeEventId(Envelope<?> envelope) {
        try {
            if (envelope == null || envelope.header() == null) return "null";
            return String.valueOf(envelope.header().eventId());
        } catch (Exception ignore) {
            return "null";
        }
    }

    private static void ackAfterCommit(Acknowledgment ack) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // 트랜잭션 없으면 즉시 ack
            ack.acknowledge();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                ack.acknowledge();
            }
        });
    }
}
