package com.back.cash.adapter.in.listener;

import com.back.cash.adapter.in.listener.exception.PayoutMessageParseException;
import com.back.cash.app.CashPayoutFacade;
import com.back.cash.app.event.CashPayoutRequestedPayload;
import com.back.cash.domain.event.CashPayoutRequestedCommand;
import com.back.cash.mapper.PayoutMapper;
import com.back.common.event.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class CashPayoutListener {

    private final CashPayoutFacade cashPayoutFacade;
    private final JsonMapper jsonMapper;

    @KafkaListener(
            topics = "${custom.kafka.topic.settlement-payout-requested}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "cashPayoutKafkaListenerContainerFactory"
    )
    public void on(String message) {
        Envelope<CashPayoutRequestedPayload> event;
        try {
            event = jsonMapper.readValue(message, new TypeReference<Envelope<CashPayoutRequestedPayload>>() {});
        } catch (Exception e) {
            log.error("[ERROR_PAYOUT_REQUESTED_CONSUME] 역직렬화 실패. message={}", message, e);
            throw new PayoutMessageParseException("payout message parse failed", e);
        }

        CashPayoutRequestedPayload data = event.payload();
        CashPayoutRequestedCommand cashPayoutRequestedCommand = PayoutMapper.toCashPayoutRequestedCommand(data);

        cashPayoutFacade.requestPayout(cashPayoutRequestedCommand);

        log.info("[PAYOUT_REQUESTED_CONSUME] 정산 완료 - settlementId: {}", data.settlementId());
    }
}
