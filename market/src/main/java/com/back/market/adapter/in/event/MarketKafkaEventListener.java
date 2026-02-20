package com.back.market.adapter.in.event;

import com.back.common.code.FailureCode;
import com.back.common.event.Envelope;
import com.back.common.exception.CustomException;
import com.back.market.app.MarketInternalFacade;
import com.back.market.event.payload.ProductCreatedPayload;
import com.back.market.event.payload.UserCreatedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketKafkaEventListener {

    private final MarketInternalFacade marketInternalFacade;
    private final JsonMapper jsonMapper;

    @KafkaListener(
            topics = "${custom.kafka.topic.user-account-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeUserCreatedEvent(String message) {
        log.info("[MarketKafkaEventListener] UserCreatedEvent 수신: {}", message);

        Envelope<UserCreatedPayload> event;

        try {
            event = jsonMapper.readValue(message, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("[MarketKafkaEventListener] UserCreatedEvent 파싱 중 오류 발생: {}", e.getMessage());
            throw new CustomException(FailureCode.INTERNAL_SERVER_ERROR);
        }

        UserCreatedPayload payload = event.payload();
        marketInternalFacade.handleUserCreatedEvent(payload);
    }

    @KafkaListener(
            topics = "${custom.kafka.topic.product-item-created}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeProductCreatedEvent(String message) {
        log.info("[MarketKafkaEventListener] product-item-created 수신: {}", message);
        Envelope<ProductCreatedPayload> event;
        try {
            event = jsonMapper.readValue(message, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("[MarketKafkaEventListener] ProductCreatedEvent 파싱 중 오류 발생: {}", e.getMessage());
            throw new CustomException(FailureCode.INTERNAL_SERVER_ERROR);
        }
        ProductCreatedPayload payload = event.payload();
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

