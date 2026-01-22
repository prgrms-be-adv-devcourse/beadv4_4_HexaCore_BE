package com.back.settlement.app.usecase;

import static com.back.settlement.domain.SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT;
import static com.back.settlement.domain.SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE;
import static com.back.settlement.domain.SettlementPolicy.PLATFORM_FEE_RATE;

import com.back.settlement.adapter.out.SettlementItemRepository;
import com.back.settlement.adapter.out.SettlementRepository;
import com.back.settlement.app.dto.internal.SettlementWithItems;
import com.back.settlement.app.dto.request.SettlementRequest;
import com.back.settlement.app.support.DomainEventPublisher;
import com.back.settlement.app.support.SettlementSupport;
import com.back.settlement.app.support.YearMonthUtils;
import com.back.settlement.domain.Settlement;
import com.back.settlement.domain.SettlementItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementCreateUseCase {
    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;
    private final SettlementSupport settlementSupport;
    private final DomainEventPublisher domainEventPublisher;
    private static final String SYSTEM_NAME = "SYSTEM";

    public SettlementWithItems createSettlementForPayee(Long payeeId, YearMonth targetMonth) {
        LocalDateTime startAt = YearMonthUtils.startOfMonth(targetMonth);
        LocalDateTime endAt = YearMonthUtils.endOfMonth(targetMonth);
        List<SettlementItem> unsettledItems = findUnsettledItems(payeeId, startAt, endAt);

        Settlement settlement = createSettlement(payeeId, unsettledItems, startAt, endAt);
        log.info("정산 생성 완료. payeeId={}, itemCount={}, netAmount={}", payeeId, unsettledItems.size(), settlement.getTotalNetAmount());

        return new SettlementWithItems(settlement, unsettledItems);
    }

    private List<SettlementItem> findUnsettledItems(Long payeeId, LocalDateTime startAt, LocalDateTime endAt) {
        List<SettlementItem> items = settlementSupport.findUnsettledItemsByPayeeId(payeeId, startAt, endAt);
        if (items.isEmpty()) {
            log.debug("{}의 미정산 항목이 없습니다.", payeeId);
        }
        return items;
    }

    private Settlement createSettlement(Long payeeId, List<SettlementItem> items, LocalDateTime startAt, LocalDateTime endAt) {
        SettlementAmounts amounts = calculateAmounts(items);
        String payeeName = extractPayeeName(items);
        SettlementRequest request = new SettlementRequest(
                payeeId,
                payeeName,
                startAt,
                endAt,
                amounts.totalSalesAmount(),
                amounts.totalFeeAmount(),
                amounts.totalNetAmount()
        );
        return Settlement.createSettlement(request);
    }

    @Transactional
    public void saveSettlementWithItems(Settlement settlement, List<SettlementItem> items) {
        Settlement savedSettlement = settlementRepository.save(settlement);
        items.forEach(item -> item.addSettlement(savedSettlement));
        settlementItemRepository.saveAll(items);
        domainEventPublisher.publishEvents(savedSettlement);
    }

    private String extractPayeeName(List<SettlementItem> items) {
        return items.stream()
                .filter(item -> item.getEventType() == SETTLEMENT_PRODUCT_SALES_AMOUNT)
                .map(SettlementItem::getSellerName)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(SYSTEM_NAME);
    }

    private SettlementAmounts calculateAmounts(List<SettlementItem> items) {
        BigDecimal salesAmount = items.stream()
                .filter(item -> item.getEventType() == SETTLEMENT_PRODUCT_SALES_AMOUNT)
                .map(SettlementItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal feeRevenue = items.stream()
                .filter(item -> item.getEventType() == SETTLEMENT_PRODUCT_SALES_FEE)
                .map(SettlementItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (salesAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal feeAmount = salesAmount.multiply(PLATFORM_FEE_RATE).setScale(0, RoundingMode.HALF_UP);
            BigDecimal netAmount = salesAmount.subtract(feeAmount);
            return new SettlementAmounts(salesAmount, feeAmount, netAmount);
        }

        return new SettlementAmounts(feeRevenue, BigDecimal.ZERO, feeRevenue);
    }

    private record SettlementAmounts(
            BigDecimal totalSalesAmount,
            BigDecimal totalFeeAmount,
            BigDecimal totalNetAmount
    ) {
    }
}
