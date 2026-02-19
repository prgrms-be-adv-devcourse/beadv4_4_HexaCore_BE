package com.back.chat.adapter.in.kafka;

import com.back.chat.adapter.in.kafka.payload.BrandDeletedPayload;
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

import static com.back.chat.adapter.in.kafka.KafkaListenerUtil.ackAfterCommit;
import static com.back.chat.adapter.in.kafka.KafkaListenerUtil.safeEventId;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaBrandDeletedListener {

    private final JsonMapper jsonMapper;
    private final ChatFacade chatFacade;

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
                    new TypeReference<Envelope<BrandDeletedPayload>>() {
                    }
            );
        } catch (Exception e) {
            log.error(
                    "[CHAT][KAFKA][DESERIALIZE_FAIL] payload={}, partition={}, offset={}",
                    json, record.partition(), record.offset(), e
            );
            // not-retryable로 설정되어 있으니: DB 로깅(Recoverer) 후 스킵
            throw new IllegalArgumentException("브랜드 삭제 이벤트 역직렬화 실패", e);
        }

        String eventIdSafe = safeEventId(envelope);

        BrandDeletedPayload payload = (envelope == null) ? null : envelope.payload();
        Long brandId = (payload == null) ? null : payload.brandId();

        if (brandId == null) {
            log.warn(
                    "[CHAT][KAFKA] invalid/empty brandId. eventId={}, partition={}, offset={}",
                    eventIdSafe, record.partition(), record.offset()
            );
            ackAfterCommit(ack);
            return;
        }

        log.info(
                "[CHAT][KAFKA] BRAND_DELETED received. eventId={}, brandId={}, partition={}, offset={}",
                eventIdSafe, brandId, record.partition(), record.offset()
        );

        try {
            int updated = chatFacade.deleteChatRoom(brandId);

            if (updated == 1) {
                log.info(
                        "[CHAT][KAFKA] chat room deleted. eventId={}, brandId={}, partition={}, offset={}",
                        eventIdSafe, brandId, record.partition(), record.offset()
                );
            } else {
                log.info(
                        "[CHAT][KAFKA] chat room already deleted (idempotent). eventId={}, brandId={}, partition={}, offset={}",
                        eventIdSafe, brandId, record.partition(), record.offset()
                );
            }

            // 트랜잭션 커밋 이후 ack
            ackAfterCommit(ack);

        } catch (Exception e) {
            log.error(
                    "[CHAT][KAFKA] chat room deletion failed. eventId={}, brandId={}, partition={}, offset={}",
                    eventIdSafe, brandId, record.partition(), record.offset(), e
            );
            throw e; // ErrorHandler가 retry/로그/스킵을 처리
        }
    }
}
