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
        BigDecimal totalNetAmount,
        Long sellerId,
        LocalDateTime occurredAt
) implements SettlementStatusChangedEvent {

    public SettlementInternalCompletedEvent(
            Long settlementId,
            SettlementStatus previousStatus,
            BigDecimal totalNetAmount,
            Long sellerId) {
        this(settlementId, previousStatus, totalNetAmount, sellerId, LocalDateTime.now());
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
