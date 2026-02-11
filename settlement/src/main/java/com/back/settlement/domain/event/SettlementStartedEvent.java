package com.back.settlement.domain.event;

import com.back.settlement.domain.SettlementStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementStartedEvent(
        Long settlementId,
        SettlementStatus previousStatus,
        String reason,
        Long sellerId,
        BigDecimal totalGrossAmount,
        BigDecimal totalNetAmount,
        BigDecimal totalFeeAmount,
        LocalDateTime occurredAt
) implements SettlementStatusChangedEvent {

    public SettlementStartedEvent(Long settlementId, SettlementStatus previousStatus, Long sellerId, BigDecimal totalGrossAmount, BigDecimal totalNetAmount, BigDecimal totalFeeAmount) {
        this(settlementId, previousStatus, "정산 캐시 지급 요청", sellerId, totalGrossAmount, totalNetAmount, totalFeeAmount, LocalDateTime.now());
    }

    @Override
    public SettlementStatus newStatus() {
        return SettlementStatus.IN_PROGRESS;
    }
}
