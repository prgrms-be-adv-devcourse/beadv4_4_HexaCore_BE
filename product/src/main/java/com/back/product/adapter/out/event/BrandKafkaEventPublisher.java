package com.back.product.adapter.out.event;

import com.back.common.event.Envelope;
import com.back.common.event.EventName;
import com.back.common.event.KafkaEventPublisher;
import com.back.product.app.usecase.ProductOutboxUseCase;
import com.back.product.domain.ProductOutboxEvent;
import com.back.product.event.kafka.*;
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
public class BrandKafkaEventPublisher {
    private final KafkaTemplate<String, EventName> kafkaTemplate;
    private final KafkaEventPublisher kafkaEventPublisher;

    private final ProductOutboxUseCase productOutboxUseCase;

    private final JsonMapper jsonMapper;

    @Value("${custom.kafka.topic.product-brand-created}")
    private String brandCreatedTopic;

    @Value("${custom.kafka.topic.product-brand-updated}")
    private String brandUpdatedTopic;

    @Value("${custom.kafka.topic.product-brand-deleted}")
    private String brandDeletedTopic;

    public void sendCreatedEvent(ProductOutboxEvent outbox) {
        String eventId = outbox.getEventId();
        try {
            BrandCreatedPayload payload = jsonMapper.readValue(outbox.getPayload(), BrandCreatedPayload.class);

            Envelope<BrandCreatedPayload> event = Envelope.of(eventId, brandCreatedTopic, payload);

            kafkaTemplate.send(brandCreatedTopic, event).whenComplete((result, exception) -> {
                if (exception == null) {
                    productOutboxUseCase.markAsSucceeded(eventId);
                    log.info("[ProductKafkaEventPublisher] Publish BrandCreatedEvent Successful. EventId: {}", eventId);
                } else {
                    productOutboxUseCase.markAsFailed(eventId);
                    log.error("[ProductKafkaEventPublisher] Publish BrandCreatedEvent Failed. EventId: {}, Error: {}", eventId, exception.getMessage(), exception);
                }
            });
        } catch (Exception e) {
            productOutboxUseCase.markAsFailed(eventId);
            log.error("[ProductKafkaEventPublisher] Publish BrandCreatedEvent Failed. EventId: {}, Error: {}", eventId, e.getMessage(), e);
        }
    }

    public void sendUpdatedEvent(ProductOutboxEvent outbox) {
        String eventId = outbox.getEventId();
        try {
            BrandUpdatedPayload payload = jsonMapper.readValue(outbox.getPayload(), BrandUpdatedPayload.class);

            Envelope<BrandUpdatedPayload> event = Envelope.of(eventId, brandUpdatedTopic, payload);

            kafkaTemplate.send(brandUpdatedTopic, event).whenComplete((result, exception) -> {
                if (exception == null) {
                    productOutboxUseCase.markAsSucceeded(eventId);
                    log.info("[ProductKafkaEventPublisher] Publish BrandUpdatedEvent Successful. EventId: {}", eventId);
                } else {
                    productOutboxUseCase.markAsFailed(eventId);
                    log.error("[ProductKafkaEventPublisher] Publish BrandUpdatedEvent Failed. EventId: {}, Error: {}", eventId, exception.getMessage(), exception);
                }
            });
        } catch (Exception e) {
            productOutboxUseCase.markAsFailed(eventId);
            log.error("[ProductKafkaEventPublisher] Publish BrandUpdatedEvent Failed. EventId: {}, Error: {}", eventId, e.getMessage(), e);
        }
    }

    public void sendDeletedEvent(ProductOutboxEvent outbox) {
        String eventId = outbox.getEventId();
        try {
            BrandDeletedPayload payload = jsonMapper.readValue(outbox.getPayload(), BrandDeletedPayload.class);

            Envelope<BrandDeletedPayload> event = Envelope.of(eventId, brandDeletedTopic, payload);

            kafkaTemplate.send(brandDeletedTopic, event).whenComplete((result, exception) -> {
                if (exception == null) {
                    productOutboxUseCase.markAsSucceeded(eventId);
                    log.info("[ProductKafkaEventPublisher] Publish BrandDeletedEvent Successful. EventId: {}", eventId);
                } else {
                    productOutboxUseCase.markAsFailed(eventId);
                    log.error("[ProductKafkaEventPublisher] Publish BrandDeletedEvent Failed. EventId: {}, Error: {}", eventId, exception.getMessage(), exception);
                }
            });
        } catch (Exception e) {
            productOutboxUseCase.markAsFailed(eventId);
            log.error("[ProductKafkaEventPublisher] Publish BrandDeletedEvent Failed. EventId: {}, Error: {}", eventId, e.getMessage(), e);
        }
    }
}
