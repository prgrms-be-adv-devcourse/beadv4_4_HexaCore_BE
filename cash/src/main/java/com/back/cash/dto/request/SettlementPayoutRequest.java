package com.back.cash.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementPayoutRequest(
        Long settlementId,
        Long payeeId,
        String payeeName,
        BigDecimal amount,
        LocalDateTime completedAt
) {
}
