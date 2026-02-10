package com.back.settlement.app.event.handler;

import com.back.common.event.Envelope;
import com.back.settlement.adapter.out.SettlementRepository;
import com.back.settlement.app.event.payload.PayoutResultPayload;
import com.back.settlement.domain.Settlement;
import tools.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class CashPayoutResultKafkaListener {
    private final SettlementRepository settlementRepository;
    private final JsonMapper jsonMapper;

    @KafkaListener(
            topics = "${custom.kafka.topic.cash-payout-completed}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void listen(String message) {
        Envelope<PayoutResultPayload> event;
        try {
            event = jsonMapper.readValue(message, new TypeReference<Envelope<PayoutResultPayload>>() {});
        } catch (Exception e) {
            log.error("캐시 지급 결과 메시지 역직렬화 실패. message={}", message, e);
            return;
        }

        PayoutResultPayload data = event.payload();
        log.info("캐시 지급 결과 수신. eventId={}, settlementId={}, success={}", event.header().eventId(), data.settlementId(), data.success());

        Settlement settlement = settlementRepository.findById(data.settlementId()).orElse(null);
        if (settlement == null) {
            log.warn("정산서를 찾을 수 없습니다. settlementId={}", data.settlementId());
            return;
        }

        if (settlement.getStatus().isTerminal()) {
            log.info("이미 정산 처리된 정산서. settlementId={}, status={}", data.settlementId(), settlement.getStatus());
            return;
        }

        if (data.success()) {
            settlement.complete();
        } else {
            settlement.fail(data.failReason());
        }
        settlementRepository.save(settlement);
    }
}
