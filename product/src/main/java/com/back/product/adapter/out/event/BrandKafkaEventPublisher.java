package com.back.product.adapter.out.event;

import com.back.common.event.Envelope;
import com.back.common.event.EventName;
import com.back.product.domain.ProductOutboxEvent;
import com.back.product.event.kafka.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BrandKafkaEventPublisher {
    private final KafkaTemplate<String, EventName> kafkaTemplate;

    private final JsonMapper jsonMapper;

    @Value("${custom.kafka.topic.product-brand-created}")
    private String brandCreatedTopic;

    @Value("${custom.kafka.topic.product-brand-updated}")
    private String brandUpdatedTopic;

    @Value("${custom.kafka.topic.product-brand-deleted}")
    private String brandDeletedTopic;

    public CompletableFuture<Void> sendCreatedEvent(ProductOutboxEvent outbox) {
        String eventId = outbox.getEventId();
        try {
            BrandCreatedPayload payload = jsonMapper.readValue(outbox.getPayload(), BrandCreatedPayload.class);

            Envelope<BrandCreatedPayload> event = Envelope.of(eventId, brandCreatedTopic, payload);

            return kafkaTemplate.send(brandCreatedTopic, event)
                    .thenRun(() -> log.info("[BrandKafkaEventPublisher] BrandCreatedEvent published"));
        } catch (Exception e) {
            log.error("[BrandKafkaEventPublisher] BrandCreatedEvent publish failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Void> sendUpdatedEvent(ProductOutboxEvent outbox) {
        String eventId = outbox.getEventId();
        try {
            BrandUpdatedPayload payload = jsonMapper.readValue(outbox.getPayload(), BrandUpdatedPayload.class);

            Envelope<BrandUpdatedPayload> event = Envelope.of(eventId, brandUpdatedTopic, payload);

            return kafkaTemplate.send(brandUpdatedTopic, event)
                    .thenRun(() -> log.info("[BrandKafkaEventPublisher] BrandUpdatedEvent published"));
        } catch (Exception e) {
            log.error("[BrandKafkaEventPublisher] BrandUpdatedEvent publish failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Void> sendDeletedEvent(ProductOutboxEvent outbox) {
        String eventId = outbox.getEventId();
        try {
            BrandDeletedPayload payload = jsonMapper.readValue(outbox.getPayload(), BrandDeletedPayload.class);

            Envelope<BrandDeletedPayload> event = Envelope.of(eventId, brandDeletedTopic, payload);

            return kafkaTemplate.send(brandDeletedTopic, event)
                    .thenRun(() -> log.info("[BrandKafkaEventPublisher] BrandDeletedEvent published"));
        } catch (Exception e) {
            log.error("[BrandKafkaEventPublisher] BrandDeletedEvent publish failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
