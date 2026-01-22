package com.back.settlement.app.event.handler;

import com.back.settlement.adapter.out.SettlementLogRepository;
import com.back.settlement.adapter.out.SettlementRepository;
import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementLog;
import com.back.settlement.domain.SettlementLog.ActorType;
import com.back.settlement.domain.event.SettlementCreatedEvent;
import com.back.settlement.domain.event.SettlementStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementLogEventHandler {
    private final SettlementLogRepository settlementLogRepository;
    private final SettlementRepository settlementRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCreated(SettlementCreatedEvent event) {
        log.debug("정산서 생성 이벤트 수신. settlementId={}, sellerId={}", event.settlementId(), event.sellerId());

        Settlement settlement = findSettlementForEvent(event);
        SettlementLog logEntry = SettlementLog.create(
                settlement,
                event.previousStatus(),
                event.newStatus(),
                event.reason(),
                ActorType.BATCH
        );

        settlementLogRepository.save(logEntry);
        log.info("정산서 생성 로그 기록 완료. settlementId={}, status={}", settlement.getId(), event.newStatus());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleStatusChanged(SettlementStatusChangedEvent event) {
        if (event instanceof SettlementCreatedEvent) {
            return;
        }
        log.debug("상태 변경 이벤트 수신. settlementId={}, {} → {}", event.settlementId(), event.previousStatus(), event.newStatus());

        Settlement settlement = settlementRepository.findById(event.settlementId()).orElse(null);

        if (settlement == null) {
            log.warn("정산서를 찾을 수 없습니다. 로그 기록 생략. settlementId={}", event.settlementId());
            return;
        }

        ActorType actorType = determineActorType(event);

        SettlementLog logEntry = SettlementLog.create(
                settlement,
                event.previousStatus(),
                event.newStatus(),
                event.reason(),
                actorType
        );

        settlementLogRepository.save(logEntry);
        log.info("상태 변경 로그 기록 완료. settlementId={}, {} → {}, reason={}",
                settlement.getId(),
                event.previousStatus(),
                event.newStatus(),
                event.reason());
    }

    private Settlement findSettlementForEvent(SettlementCreatedEvent event) {
        if (event.settlementId() != null) {
            return settlementRepository.findById(event.settlementId()).orElse(null);
        }

        return settlementRepository.findBySellerId(event.sellerId())
                .stream()
                .filter(s -> s.getStatus() == event.newStatus())
                .findFirst()
                .orElse(null);
    }

    private ActorType determineActorType(SettlementStatusChangedEvent event) {
        // TODO: 요청 컨텍스트(예: SecurityContext)를 확인하여 ActorType 결정
        return ActorType.BATCH;
    }
}
