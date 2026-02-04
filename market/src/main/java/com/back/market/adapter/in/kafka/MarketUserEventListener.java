package com.back.market.adapter.in.kafka;

import com.back.common.code.FailureCode;
import com.back.common.exception.CustomException;
import com.back.common.user.event.UserCreatedEvent;
import com.back.market.app.MarketInternalFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketUserEventListener {

    private final MarketInternalFacade marketInternalFacade;

    @KafkaListener(
            topics = "${custom.kafka.topic.user-created}",
            groupId = "${custom.kafka.consumer.group-id}"
    )
    public void consume(UserCreatedEvent event) {
        log.info("[MarketUserEventListener] UserCreatedEvent 수신: {}", event.id());

        try {
            if (event.id() == null || event.email() == null) {
                log.error("[MarketUserEventListener] 잘못된 UserCreatedEvent 수신: id={}, email={}", event.id(), event.email());
                throw new CustomException(FailureCode.MISSING_REQUIRED_FIELD);
            }
            marketInternalFacade.handleUserCreatedEvent(event);
            log.info("[MarketUserEventListener] UserCreatedEvent 처리 완료: id={}", event.id());
        } catch (CustomException e) {
            log.error("[MarketUserEventListener] 비즈니스 예외 발생: id={}, code={}, message={}", event.id(), e.getFailureCode().getCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[MarketUserEventListener] UserCreatedEvent 처리 중 오류 발생: id={}, error={}", event.id(), e.getMessage());
            throw new CustomException(FailureCode.INTERNAL_SERVER_ERROR);
        }

    }

}
