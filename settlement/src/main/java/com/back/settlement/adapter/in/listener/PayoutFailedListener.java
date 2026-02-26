package com.back.settlement.adapter.in.listener;

import com.back.common.event.Envelope;
import com.back.settlement.app.event.payload.PayoutFailedPayload;
import com.back.settlement.app.usecase.SettlementPayoutFailUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayoutFailedListener {

    private final SettlementPayoutFailUseCase settlementPayoutFailUseCase;
    private final JsonMapper jsonMapper;

    @KafkaListener(
            topics = "${custom.kafka.topic.cash-payout-failed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "payoutFailedKafkaListenerContainerFactory"
    )
    public void on(String message) {
        Envelope<PayoutFailedPayload> event;
        try {
            event = jsonMapper.readValue(message, new TypeReference<Envelope<PayoutFailedPayload>>() {});
        } catch (Exception e) {
            log.error("[ERROR_PAYOUT_FAILED_CONSUME] 역직렬화 실패. message={}", message, e);
            throw new MessageConversionException("payout failed message parse failed", e);
        }

        PayoutFailedPayload data = event.payload();
        settlementPayoutFailUseCase.markAsFailed(data.settlementId(), data.reason());
        log.info("[PAYOUT_FAILED_CONSUME] 정산 실패 처리 완료 - settlementId: {}", data.settlementId());
    }
}
