package com.back.product.adapter.out.event;

import com.back.common.event.Envelope;
import com.back.common.event.EventName;
import com.back.common.event.KafkaEventPublisher;
import com.back.product.app.usecase.ProductOutboxUseCase;
import com.back.product.domain.ProductOutboxEvent;
import com.back.product.event.kafka.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import tools.jackson.databind.json.JsonMapper;

import java.util.stream.Collectors;

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

    public void sendUpdatedEvent(@Valid BrandUpdatedPayload payload) {
        Envelope<BrandUpdatedPayload> event = Envelope.of(brandUpdatedTopic, payload);

        kafkaEventPublisher.publish(brandUpdatedTopic, event);

        log.info("[BrandKafkaEventPublisher] Sent BrandUpdatedPayload brandName: {}", payload.brand().name());
    }

    public void sendDeletedEvent(@Valid BrandDeletedPayload payload) {
        Envelope<BrandDeletedPayload> event = Envelope.of(brandDeletedTopic, payload);

        kafkaEventPublisher.publish(brandDeletedTopic, event);

        log.info("[BrandKafkaEventPublisher] Sent BrandDeletedPayload brandId : {}", payload.brandId());
    }
}
