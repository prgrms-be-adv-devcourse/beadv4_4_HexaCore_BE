package com.back.market.adapter.in.event;

import com.back.common.event.KafkaEventParser;
import com.back.market.app.MarketInternalFacade;
import com.back.market.event.payload.ProductCreatedPayload;
import com.back.market.event.payload.UserCreatedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import tools.jackson.core.type.TypeReference;

@Slf4j
@Component
@Validated
@RequiredArgsConstructor
public class MarketKafkaEventListener {

    private final MarketInternalFacade marketInternalFacade;
    private final KafkaEventParser kafkaEventParser;

    @KafkaListener(
            topics = "${custom.kafka.topic.user-account-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeUserCreatedEvent(String message) {
        log.info("[MarketKafkaEventListener] UserCreatedEvent 수신: {}", message);
        UserCreatedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        marketInternalFacade.handleUserCreatedEvent(payload);
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.product-item-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeProductCreatedEvent(String message) {
        log.info("[MarketKafkaEventListener] product-item-created 수신: {}", message);
        ProductCreatedPayload payload = kafkaEventParser.extractPayload(message, new TypeReference<>() {});
        marketInternalFacade.handleProductCreatedEvent(payload);
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.product-item-updated}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeProductUpdatedEvent(String message) {
        log.info("[MarketKafkaEventListener] product-item-updated 수신: {}", message);
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.product-item-deleted}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeProductDeletedEvent(String message) {
        log.info("[MarketKafkaEventListener] product-item-deleted 수신: {}", message);
    }

}

