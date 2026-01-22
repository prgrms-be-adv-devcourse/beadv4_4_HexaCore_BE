package com.back.settlement.domain.event;

import com.back.settlement.domain.SettlementStatus;
import java.time.LocalDateTime;

public record SettlementStartedEvent(
        Long settlementId,
        SettlementStatus previousStatus,
        String reason,
        LocalDateTime occurredAt
) implements SettlementStatusChangedEvent {

    public SettlementStartedEvent(Long settlementId, SettlementStatus previousStatus) {
        this(settlementId, previousStatus, "정산 처리 시작", LocalDateTime.now());
    }

    @Override
    public SettlementStatus newStatus() {
        return SettlementStatus.IN_PROGRESS;
    }
}
