package com.back.market.adapter.in.event;

import com.back.common.event.KafkaEventParser;
import com.back.market.app.MarketInternalFacade;
import com.back.market.event.payload.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketKafkaEventListener {

    private final MarketInternalFacade marketInternalFacade;
    private final KafkaEventParser kafkaEventParser;

    @KafkaListener(
            topics = "${custom.kafka.topic.user-account-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeUserCreatedEvent(String message) {
        UserCreatedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        marketInternalFacade.handleUserCreatedEvent(payload);
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.user-account-updated}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeUserUpdatedEvent(String message) {
        UserUpdatedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        marketInternalFacade.handleUserUpdatedEvent(payload);
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.product-item-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeProductCreatedEvent(String message) {
        ProductCreatedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        marketInternalFacade.handleProductCreatedEvent(payload);
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.product-item-updated}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeProductUpdatedEvent(String message) {
        ProductUpdatedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        marketInternalFacade.handleProductUpdatedEvent(payload);
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.product-item-deleted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeProductDeletedEvent(String message) {
        ProductDeletedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        marketInternalFacade.handleProductDeletedEvent(payload);
    }

}

