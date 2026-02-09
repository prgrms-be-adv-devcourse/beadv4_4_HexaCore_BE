package com.back.settlement.app.usecase;

import com.back.settlement.adapter.out.SettlementItemRepository;
import com.back.settlement.adapter.out.SettlementRepository;
import com.back.settlement.app.support.DomainEventPublisher;
import com.back.settlement.app.support.LocalDateUtils;
import com.back.settlement.app.support.SettlementSupport;
import com.back.settlement.batch.SettlementWithItems;
import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementItem;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementCreateUseCase {
    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;
    private final SettlementSupport settlementSupport;
    private final DomainEventPublisher domainEventPublisher;

    public SettlementWithItems createSettlement(Long payeeId, YearMonth targetMonth) {
        LocalDateTime startAt = LocalDateUtils.startOfMonth(targetMonth);
        LocalDateTime endAt = LocalDateUtils.endOfMonth(targetMonth);
        List<SettlementItem> items = settlementSupport.findUnsettledItems(payeeId, startAt, endAt);

        Settlement settlement = Settlement.create(payeeId, items, startAt, endAt);
        log.info("정산 생성 완료. payeeId={}, itemCount={}, netAmount={}", payeeId, items.size(), settlement.getTotalNetAmount());

        return new SettlementWithItems(settlement, items);
    }

    @Transactional
    public void saveSettlement(Settlement settlement, List<SettlementItem> items) {
        Settlement saved = settlementRepository.save(settlement);
        items.forEach(item -> {
            item.addSettlement(saved);
            item.included();
        });
        settlementItemRepository.saveAll(items);
        saved.start();
        domainEventPublisher.publishEvents(saved);
    }
}
