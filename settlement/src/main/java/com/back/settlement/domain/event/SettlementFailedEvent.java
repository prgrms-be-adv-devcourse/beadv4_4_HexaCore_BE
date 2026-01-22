package com.back.settlement.domain.event;

import com.back.settlement.domain.SettlementStatus;
import java.time.LocalDateTime;

public record SettlementFailedEvent(
        Long settlementId,
        SettlementStatus previousStatus,
        String reason,
        LocalDateTime occurredAt
) implements SettlementStatusChangedEvent {

    public SettlementFailedEvent(Long settlementId, SettlementStatus previousStatus, String reason) {
        this(settlementId, previousStatus, reason, LocalDateTime.now());
    }

    @Override
    public SettlementStatus newStatus() {
        return SettlementStatus.FAILED;
    }
}
