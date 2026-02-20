package com.back.settlement.domain.event;

import com.back.settlement.domain.SettlementStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 정산 완료 이벤트 (모듈 내부용).
 */
public record SettlementInternalCompletedEvent(
        Long settlementId,
        SettlementStatus previousStatus,
        BigDecimal totalSalesAmount,
        BigDecimal totalFeeAmount,
        BigDecimal totalNetAmount,
        Long sellerId,
        String sellerName,
        LocalDateTime completedAt,
        LocalDateTime occurredAt
) implements SettlementStatusChangedEvent {

    public SettlementInternalCompletedEvent(
            Long settlementId,
            SettlementStatus previousStatus,
            BigDecimal totalSalesAmount,
            BigDecimal totalFeeAmount,
            BigDecimal totalNetAmount,
            Long sellerId,
            String sellerName,
            LocalDateTime completedAt) {
        this(settlementId, previousStatus, totalSalesAmount, totalFeeAmount, totalNetAmount, sellerId, sellerName, completedAt, LocalDateTime.now());
    }

    @Override
    public SettlementStatus newStatus() {
        return SettlementStatus.COMPLETED;
    }

    @Override
    public String reason() {
        return "정산 완료";
    }
}
