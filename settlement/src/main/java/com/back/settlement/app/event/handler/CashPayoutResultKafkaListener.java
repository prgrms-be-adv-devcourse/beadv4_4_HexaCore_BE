package com.back.settlement.app.event.handler;

import com.back.common.event.Envelope;
import com.back.settlement.adapter.out.SettlementRepository;
import com.back.settlement.app.event.payload.PayoutResultPayload;
import com.back.settlement.app.support.DomainEventPublisher;
import com.back.settlement.domain.Settlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CashPayoutResultKafkaListener {
    private final SettlementRepository settlementRepository;
    private final DomainEventPublisher domainEventPublisher;

    @KafkaListener(
            topics = "${custom.kafka.topic.cash-payout-completed}",
            groupId = "${custom.kafka.consumer.group-id}",
            properties = "spring.json.value.default.type=com.back.common.event.Envelope"
    )
    @Transactional
    public void listen(Envelope<PayoutResultPayload> event) {
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
            domainEventPublisher.publishEvents(settlement);
            log.info("정산 완료 처리. settlementId={}", data.settlementId());
        } else {
            settlement.fail(data.failReason());
            domainEventPublisher.publishEvents(settlement);
            log.info("정산 실패 처리. settlementId={}, reason={}", data.settlementId(), data.failReason());
        }
    }
}
