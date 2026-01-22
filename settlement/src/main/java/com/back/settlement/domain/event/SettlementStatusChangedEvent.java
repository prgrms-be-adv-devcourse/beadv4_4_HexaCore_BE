package com.back.settlement.domain.event;

import com.back.settlement.domain.SettlementStatus;

public sealed interface SettlementStatusChangedEvent extends SettlementEvent permits SettlementCreatedEvent, SettlementStartedEvent, SettlementInternalCompletedEvent, SettlementFailedEvent, SettlementHoldEvent {
    SettlementStatus previousStatus();
    SettlementStatus newStatus();
    String reason();
}
