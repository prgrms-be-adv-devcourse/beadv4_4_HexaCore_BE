package com.back.cash.domain.event;

public record PayoutFailedEvent (
        Long settlementId,
        String reason
) {
}
