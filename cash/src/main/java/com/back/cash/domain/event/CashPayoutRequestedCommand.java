package com.back.cash.domain.event;

import com.back.common.event.EventName;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CashPayoutRequestedCommand(
        Long settlementId,
        Long payeeId,
        BigDecimal totalGrossAmount,
        BigDecimal totalNetAmount,
        BigDecimal totalFeeAmount
) implements EventName {}
