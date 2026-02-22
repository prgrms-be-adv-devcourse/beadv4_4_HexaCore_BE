package com.back.product.adapter.out.event;

import com.back.common.event.Envelope;
import com.back.common.event.EventName;
import com.back.product.app.usecase.ProductOutboxUseCase;
import com.back.product.domain.ProductOutboxEvent;
import com.back.product.event.kafka.ProductCreatedPayload;
import com.back.product.event.kafka.ProductDeletedPayload;
import com.back.product.event.kafka.ProductUpdatedPayload;
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
public class ProductKafkaEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final JsonMapper jsonMapper;

    @Value("${custom.kafka.topic.product-item-created}")
    private String productCreatedTopic;

    @Value("${custom.kafka.topic.product-item-updated}")
    private String productUpdatedTopic;

    @Value("${custom.kafka.topic.product-item-deleted}")
    private String productDeletedTopic;

    public CompletableFuture<Void> sendCreatedEvent(ProductOutboxEvent outbox) {
        String eventId = outbox.getEventId();
        try {
            ProductCreatedPayload payload = jsonMapper.readValue(outbox.getPayload(), ProductCreatedPayload.class);

            Envelope<ProductCreatedPayload> event = Envelope.of(eventId, productCreatedTopic, payload);

            return kafkaTemplate.send(productCreatedTopic, event)
                    .thenRun(() -> log.info("[ProductKafkaEventPublisher] ProductCreatedEvent published"));
        } catch (Exception e) {
            log.error("[ProductKafkaEventPublisher] ProductCreatedEvent publish failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Void> sendModifiedEvent(ProductOutboxEvent outbox) {
        String eventId = outbox.getEventId();
        try {
            ProductUpdatedPayload payload = jsonMapper.readValue(outbox.getPayload(), ProductUpdatedPayload.class);

            Envelope<ProductUpdatedPayload> event = Envelope.of(eventId, productUpdatedTopic, payload);

            return kafkaTemplate.send(productUpdatedTopic, event)
                    .thenRun(() -> log.info("[ProductKafkaEventPublisher] ProductUpdatedEvent published"));
        } catch (Exception e) {
            log.error("[ProductKafkaEventPublisher] ProductUpdatedEvent publish failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Void> sendDeletedEvent(ProductOutboxEvent outbox) {
        String eventId = outbox.getEventId();
        try {
            ProductDeletedPayload payload = jsonMapper.readValue(outbox.getPayload(), ProductDeletedPayload.class);

            Envelope<ProductDeletedPayload> event = Envelope.of(eventId, productDeletedTopic, payload);

            return kafkaTemplate.send(productDeletedTopic, event)
                    .thenRun(() -> log.info("[ProductKafkaEventPublisher] ProductDeletedEvent published"));
        } catch (Exception e) {
            log.error("[ProductKafkaEventPublisher] ProductDeletedEvent publish failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
