package com.back.cash.mapper;

import com.back.cash.domain.Payout;
import com.back.cash.domain.enums.PayoutStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PayoutMapper {
    public static Payout toPayout(Long settlementId, Long payeeId, BigDecimal amount, LocalDateTime completedAt) {
        return Payout.builder()
                .settlementId(settlementId)
                .payeeId(payeeId)
                .amount(amount)
                .completedAt(completedAt)
                .status(PayoutStatus.PROCESSING)
                .build();
    }
}
