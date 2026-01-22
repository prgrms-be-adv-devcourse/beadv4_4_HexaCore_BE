package com.back.settlement.app.usecase;

import com.back.settlement.adapter.out.SettlementRepository;
import com.back.settlement.adapter.out.feign.cash.CashClient;
import com.back.settlement.app.dto.internal.SettlementWithPayout;
import com.back.settlement.app.event.SettlementPayoutRequest;
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
    private final CashClient cashClient;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public SettlementWithPayout completeSettlement(Settlement settlement) {
        settlement.start();
        settlement.complete();

        SettlementPayoutRequest payoutRequest = new SettlementPayoutRequest(
                settlement.getId(),
                settlement.getSellerId(),
                settlement.getSellerName(),
                settlement.getTotalNetAmount(),
                settlement.getCompletedAt()
        );

        return new SettlementWithPayout(settlement, payoutRequest);
    }

    @Transactional
    public void saveAndRequestPayout(List<SettlementWithPayout> settlementWithPayouts) {
        List<SettlementPayoutRequest> payouts = settlementWithPayouts.stream()
                .map(item -> {
                    Settlement saved = settlementRepository.save(item.settlement());
                    domainEventPublisher.publishEvents(saved);
                    return item.payoutRequest();
                })
                .toList();
        cashClient.requestPayout(payouts);
        log.info("정산 완료 및 캐시 지급 요청. 건수={}", payouts.size());
    }
}
