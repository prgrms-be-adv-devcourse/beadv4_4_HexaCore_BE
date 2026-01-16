package com.back.common.Settlement.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementCompletedEvent(
        Long sellerId,
        LocalDateTime startAt,
        LocalDateTime endAt,
        BigDecimal totalNetAmount
) {
}
