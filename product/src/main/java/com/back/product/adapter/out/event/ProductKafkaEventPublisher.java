package com.back.product.adapter.out.event;

import com.back.common.event.Envelope;
import com.back.common.event.EventName;
import com.back.common.event.KafkaEventPublisher;
import com.back.common.exception.CustomException;
import com.back.product.app.usecase.ProductOutboxUseCase;
import com.back.product.domain.ProductOutboxEvent;
import com.back.product.event.kafka.ProductCreatedPayload;
import com.back.product.event.kafka.ProductDeletedPayload;
import com.back.product.event.kafka.ProductUpdatedPayload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ProductKafkaEventPublisher {
    private final KafkaTemplate<String, EventName> kafkaTemplate;
    private final KafkaEventPublisher kafkaEventPublisher;

    private final ProductOutboxUseCase productOutboxUseCase;

    private final JsonMapper jsonMapper;

    @Value("${custom.kafka.topic.product-item-created}")
    private String productCreatedTopic;

    @Value("${custom.kafka.topic.product-item-updated}")
    private String productUpdatedTopic;

    @Value("${custom.kafka.topic.product-item-deleted}")
    private String productDeletedTopic;

    public void sendCreatedEvent(ProductOutboxEvent outbox) {
        String eventId = outbox.getEventId();
        try {
            ProductCreatedPayload payload = jsonMapper.readValue(outbox.getPayload(), ProductCreatedPayload.class);

            Envelope<ProductCreatedPayload> event = Envelope.of(eventId, productCreatedTopic, payload);

            kafkaTemplate.send(productCreatedTopic, event).whenComplete((result, exception) -> {
                if (exception == null) {
                    productOutboxUseCase.markAsSucceeded(eventId);
                    log.info("[ProductKafkaEventPublisher] Publish ProductCreatedEvent Successful. EventId: {}", eventId);
                } else {
                    productOutboxUseCase.markAsFailed(eventId);
                    log.error("[ProductKafkaEventPublisher] Publish ProductCreatedEvent Failed. EventId: {}, Error: {}", eventId, exception.getMessage(), exception);
                }
            });
        } catch (Exception e) {
            productOutboxUseCase.markAsFailed(eventId);
            log.error("[ProductKafkaEventPublisher] Publish ProductCreatedEvent Failed. EventId: {}, Error: {}", eventId, e.getMessage(), e);
        }
    }

    public void sendModifiedEvent(ProductOutboxEvent outbox) {
        String eventId = outbox.getEventId();
        try {
            ProductUpdatedPayload payload = jsonMapper.readValue(outbox.getPayload(), ProductUpdatedPayload.class);

            Envelope<ProductUpdatedPayload> event = Envelope.of(eventId, productUpdatedTopic, payload);

            kafkaTemplate.send(productUpdatedTopic, event).whenComplete((result, exception) -> {
                if (exception == null) {
                    productOutboxUseCase.markAsSucceeded(eventId);
                    log.info("[ProductKafkaEventPublisher] Publish ProductUpdatedEvent Successful. EventId: {}", eventId);
                } else {
                    productOutboxUseCase.markAsFailed(eventId);
                    log.error("[ProductKafkaEventPublisher] Publish ProductUpdatedEvent Failed. EventId: {}, Error: {}", eventId, exception.getMessage(), exception);
                }
            });
        } catch (Exception e) {
            productOutboxUseCase.markAsFailed(eventId);
            log.error("[ProductKafkaEventPublisher] Publish ProductUpdatedEvent Failed. EventId: {}, Error: {}", eventId, e.getMessage(), e);
        }
    }

    public void sendDeletedEvent(ProductOutboxEvent outbox) {
        String eventId = outbox.getEventId();
        try {
            ProductDeletedPayload payload = jsonMapper.readValue(outbox.getPayload(), ProductDeletedPayload.class);

            Envelope<ProductDeletedPayload> event = Envelope.of(eventId, productDeletedTopic, payload);

            kafkaTemplate.send(productDeletedTopic, event).whenComplete((result, exception) -> {
                if (exception == null) {
                    productOutboxUseCase.markAsSucceeded(eventId);
                    log.info("[ProductKafkaEventPublisher] Publish ProductDeletedEvent Successful. EventId: {}", eventId);
                } else {
                    productOutboxUseCase.markAsFailed(eventId);
                    log.error("[ProductKafkaEventPublisher] Publish ProductDeletedEvent Failed. EventId: {}, Error: {}", eventId, exception.getMessage(), exception);
                }
            });
        } catch (Exception e) {
            productOutboxUseCase.markAsFailed(eventId);
            log.error("[ProductKafkaEventPublisher] Publish ProductDeletedEvent Failed. EventId: {}, Error: {}", eventId, e.getMessage(), e);
        }
    }
}
