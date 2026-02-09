package com.back.settlement.domain;

import static com.back.settlement.domain.SettlementEventType.SETTLEMENT_PRODUCT_SALES_AMOUNT;
import static com.back.settlement.domain.SettlementEventType.SETTLEMENT_PRODUCT_SALES_FEE;

import java.math.BigDecimal;
import java.util.List;

public record SettlementAmounts(
        BigDecimal totalSalesAmount,
        BigDecimal totalFeeAmount,
        BigDecimal totalNetAmount
) {
    public static SettlementAmounts fromItems(List<SettlementItem> items) {
        BigDecimal salesAmount = items.stream()
                .filter(item -> item.getEventType() == SETTLEMENT_PRODUCT_SALES_AMOUNT)
                .map(SettlementItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal feeAmount = items.stream()
                .filter(item -> item.getEventType() == SETTLEMENT_PRODUCT_SALES_FEE)
                .map(SettlementItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (salesAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal netAmount = salesAmount.subtract(feeAmount);
            return new SettlementAmounts(salesAmount, feeAmount, netAmount);
        }

        return new SettlementAmounts(feeAmount, BigDecimal.ZERO, feeAmount);
    }
}
