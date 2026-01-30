package com.back.settlement.app.usecase;

import com.back.settlement.adapter.out.SettlementRepository;
import com.back.settlement.app.support.DomainEventPublisher;
import com.back.settlement.domain.Settlement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementCompleteUseCase {
    private final SettlementRepository settlementRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public Settlement completeSettlement(Settlement settlement) {
        settlement.start();
        settlement.complete();
        return settlement;
    }

    @Transactional
    public void saveAndPublishEvents(List<Settlement> settlements) {
        settlements.forEach(settlement -> {
            Settlement saved = settlementRepository.save(settlement);
            domainEventPublisher.publishEvents(saved);
        });
    }
}