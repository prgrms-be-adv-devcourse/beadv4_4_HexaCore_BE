package com.back.settlement.app.usecase;

import static com.back.settlement.domain.SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT;
import static com.back.settlement.domain.SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE;

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
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${settlement.system-payee-id}")
    private Long systemPayeeId;

    public List<SettlementWithItems> createSettlements(List<Long> payeeIds, YearMonth targetMonth) {
        LocalDateTime startAt = LocalDateUtils.startOfMonth(targetMonth);
        LocalDateTime endAt = LocalDateUtils.endOfMonth(targetMonth);

        List<SettlementItem> allItems = settlementSupport.findUnsettledItemsByPayeeIds(payeeIds, startAt, endAt);

        // 청크에 시스템 계좌가 없으면 수수료 항목을 별도 조회
        boolean hasSystemPayee = payeeIds.contains(systemPayeeId);
        List<SettlementItem> feeItems;
        if (hasSystemPayee) {
            feeItems = allItems.stream()
                    .filter(item -> item.getEventType() == SETTLEMENT_PRODUCT_SALES_FEE)
                    .toList();
        } else {
            feeItems = settlementSupport.findUnsettledItemsByPayeeIds(
                    List.of(systemPayeeId), startAt, endAt);
        }

        Map<Long, List<SettlementItem>> itemsByPayee = allItems.stream()
                .collect(Collectors.groupingBy(SettlementItem::getPayeeId));

        Map<Long, SettlementItem> feeItemsByOrderId = feeItems.stream()
                .filter(item -> item.getEventType() == SETTLEMENT_PRODUCT_SALES_FEE)
                .collect(Collectors.toMap(SettlementItem::getOrderId, item -> item, (a, b) -> a));

        return payeeIds.stream()
                .map(payeeId -> itemsByPayee.getOrDefault(payeeId, List.of()))
                .filter(items -> !items.isEmpty())
                .map(items -> {
                    Long payeeId = items.get(0).getPayeeId();

                    Settlement settlement;
                    if (!payeeId.equals(systemPayeeId)) {
                        List<SettlementItem> matchedFees = items.stream()
                                .filter(item -> item.getEventType() == SETTLEMENT_PRODUCT_SALES_AMOUNT)
                                .map(item -> feeItemsByOrderId.get(item.getOrderId()))
                                .filter(Objects::nonNull)
                                .toList();
                        settlement = Settlement.createForSeller(payeeId, items, matchedFees, startAt, endAt);
                    } else {
                        settlement = Settlement.createForSystem(payeeId, items, startAt, endAt);
                    }
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
