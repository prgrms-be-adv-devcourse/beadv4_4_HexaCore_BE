package com.back.settlement.app.support;

import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.event.SettlementEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public void publishEvents(Settlement settlement) {
        if (settlement.getDomainEvents().isEmpty()) {
            log.debug("발행할 이벤트가 없습니다. settlementId={}", settlement.getId());
            return;
        }

        log.debug("도메인 이벤트 발행 시작. settlementId={}, eventCount={}", settlement.getId(), settlement.getDomainEvents().size());

        settlement.getDomainEvents().forEach(event -> {
            log.debug("이벤트 발행: {}", event.getClass().getSimpleName());
            eventPublisher.publishEvent(event);
        });
        settlement.clearDomainEvents();
        log.debug("도메인 이벤트 발행 완료. settlementId={}", settlement.getId());
    }
}
