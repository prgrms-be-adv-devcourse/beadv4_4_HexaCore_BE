package com.back.product.adapter.out.event;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.product.event.kafka.ProductCreatedPayload;
import com.back.product.event.kafka.ProductDeletedPayload;
import com.back.product.event.kafka.ProductUpdatedPayload;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ProductKafkaEventPublisher {
    private final KafkaEventPublisher kafkaEventPublisher;

    @Value("${custom.kafka.topic.product-item-created}")
    private String productCreatedTopic;

    @Value("${custom.kafka.topic.product-item-updated}")
    private String productUpdatedTopic;

    @Value("${custom.kafka.topic.product-item-deleted}")
    private String productDeletedTopic;

    public void sendCreatedEvent(@Valid ProductCreatedPayload payload) {
        Envelope<ProductCreatedPayload> event = Envelope.of(productCreatedTopic, payload);

        kafkaEventPublisher.publish(productCreatedTopic, event);

        log.info("[ProductKafkaEventPublisher] Sent ProductCreatedPayload for productInfoId: {}", payload.productInfo().productInfoId());
    }

    public void sendModifiedEvent(@Valid ProductUpdatedPayload payload) {
        Envelope<ProductUpdatedPayload> event = Envelope.of(productUpdatedTopic, payload);

        kafkaEventPublisher.publish(productUpdatedTopic, event);

        log.info("[ProductKafkaEventPublisher] Sent ProductUpdatedPayload for productInfoId: {}", payload.productInfo().productInfoId());
    }

    public void sendDeletedEvent(@Valid ProductDeletedPayload payload) {
        Envelope<ProductDeletedPayload> event = Envelope.of(productDeletedTopic, payload);

        kafkaEventPublisher.publish(productDeletedTopic, event);

        log.info("[ProductKafkaEventPublisher] Sent ProductDeletedPayload for productInfoId: {}", payload.productInfoId());
    }
}
