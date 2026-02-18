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
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaBrandCreatedListener {

    private final JsonMapper jsonMapper;
    private final ChatFacade chatFacade;

    @KafkaListener(
            topics = "${custom.kafka.topic.product-brand-created:product.brand.created}",
            groupId = "${spring.kafka.consumer.group-id}"
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
                    new TypeReference<Envelope<BrandCreatedPayload>>() {
                    }
            );
        } catch (Exception e) {
            log.error(
                    "[CHAT][KAFKA][DESERIALIZE_FAIL] payload={}, partition={}, offset={}",
                    json,
                    record.partition(),
                    record.offset(),
                    e
            );
            throw new IllegalArgumentException("브랜드 생성 이벤트 역직렬화 실패", e);
        }

        if (envelope.payload() == null || envelope.payload().brandId() == null) {
            log.error("[CHAT][KAFKA] invalid payload. eventId={}", envelope.header().eventId());
            ack.acknowledge();
            return;
        }

        Long brandId = envelope.payload().brandId();

        log.info(
                "[CHAT][KAFKA] BRAND_CREATED received. eventId={}, brandId={}, partition={}, offset={}",
                envelope.header().eventId(),
                brandId,
                record.partition(),
                record.offset()
        );

        try {
            chatFacade.createChatRoomIfNotExists(brandId);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[CHAT][KAFKA] chat room creation failed. brandId={}", brandId, e);
            throw e;
        }
    }
}
