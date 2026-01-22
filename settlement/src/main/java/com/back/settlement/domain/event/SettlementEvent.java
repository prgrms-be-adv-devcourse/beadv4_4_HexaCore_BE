package com.back.settlement.domain.event;

import java.time.LocalDateTime;

public sealed interface SettlementEvent permits SettlementStatusChangedEvent {
    Long settlementId();
    LocalDateTime occurredAt();
}
