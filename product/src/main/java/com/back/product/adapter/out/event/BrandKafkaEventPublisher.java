package com.back.product.adapter.out.event;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.product.event.kafka.BrandCreatedPayload;
import com.back.product.event.kafka.BrandPayload;
import com.back.product.event.kafka.BrandUpdatedPayload;
import com.back.product.event.kafka.ProductCreatedPayload;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BrandKafkaEventPublisher {
    private final KafkaEventPublisher kafkaEventPublisher;

    @Value("${custom.kafka.topic.product-brand-created}")
    private String brandCreatedTopic;

    @Value("${custom.kafka.topic.product-brand-updated}")
    private String brandUpdatedTopic;

    public void sendCreatedEvent(@Valid BrandCreatedPayload payload) {
        Envelope<BrandCreatedPayload> event = Envelope.of(brandCreatedTopic, payload);

        kafkaEventPublisher.publish(brandCreatedTopic, event);

        log.info("[BrandKafkaEventPublisher] Sent BrandCreatedPayload size: {}, brands: {}",
                payload.brandPayloadList().size(),
                payload.brandPayloadList().stream()
                        .map(BrandPayload::name)
                        .collect(Collectors.joining(", "))
        );
    }

    public void sendUpdatedEvent(@Valid BrandUpdatedPayload payload) {
        Envelope<BrandUpdatedPayload> event = Envelope.of(brandUpdatedTopic, payload);

        kafkaEventPublisher.publish(brandUpdatedTopic, event);

        log.info("[BrandKafkaEventPublisher] Sent BrandUpdatedPayload brandName: {}", payload.brandPayload().name());
    }
}
