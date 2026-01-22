package com.back.settlement.domain.event;

import com.back.settlement.domain.SettlementStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementCreatedEvent(
        Long settlementId,
        Long sellerId,
        BigDecimal totalNetAmount,
        LocalDateTime occurredAt
) implements SettlementStatusChangedEvent {

    public SettlementCreatedEvent(Long settlementId, Long sellerId, BigDecimal totalNetAmount) {
        this(settlementId, sellerId, totalNetAmount, LocalDateTime.now());
    }

    @Override
    public SettlementStatus previousStatus() {
        return null;
    }

    @Override
    public SettlementStatus newStatus() {
        return SettlementStatus.PENDING;
    }

    @Override
    public String reason() {
        return "정산서 생성";
    }
}
