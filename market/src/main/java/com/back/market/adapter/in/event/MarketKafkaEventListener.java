package com.back.market.adapter.in.event;

import com.back.common.code.FailureCode;
import com.back.common.event.Envelope;
import com.back.common.exception.CustomException;
import com.back.market.app.MarketInternalFacade;
import com.back.market.event.payload.UserCreatedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

}

