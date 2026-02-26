package com.back.settlement.app.usecase;

import com.back.settlement.adapter.out.SettlementItemRepository;
import com.back.settlement.adapter.out.SettlementRepository;
import com.back.settlement.app.support.LocalDateUtils;
import com.back.settlement.app.support.SettlementSupport;
import com.back.settlement.batch.SettlementWithItems;
import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementItem;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    public List<SettlementWithItems> createSettlements(List<Long> payeeIds, YearMonth targetMonth) {
        LocalDateTime startAt = LocalDateUtils.startOfMonth(targetMonth);
        LocalDateTime endAt = LocalDateUtils.endOfMonth(targetMonth);

        List<SettlementItem> allItems = settlementSupport.findUnsettledItemsByPayeeIds(payeeIds, startAt, endAt);

        Map<Long, List<SettlementItem>> itemsByPayee = allItems.stream()
                .collect(Collectors.groupingBy(SettlementItem::getPayeeId));

        return payeeIds.stream()
                .map(payeeId -> itemsByPayee.getOrDefault(payeeId, List.of()))
                .filter(items -> !items.isEmpty())
                .map(items -> {
                    Settlement settlement = Settlement.create(items.get(0).getPayeeId(), items, startAt, endAt);
                    return new SettlementWithItems(settlement, items);
                })
                .toList();
    }

    @Transactional
    public void saveSettlement(Settlement settlement, List<SettlementItem> items) {
        Settlement saved = settlementRepository.save(settlement);
        items.forEach(item -> {
            item.addSettlement(saved);
            item.included();
        });
        settlementItemRepository.saveAll(items);
        saved.complete(); // PENDING -> COMPLETED, 도메인 이벤트 등록
        settlementRepository.save(saved); // AbstractAggregateRoot의 도메인 이벤트 발행을 위해 save() 필수 (dirty checking만으로는 이벤트 미발행)
        log.info("정산 생성 및 확정 완료. settlementId={}", saved.getId());
    }
}
