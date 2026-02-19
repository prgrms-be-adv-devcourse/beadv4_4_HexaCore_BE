package com.back.cash.app.event;

import com.back.common.event.KafkaPayload;

import java.math.BigDecimal;

public record CashPayoutRequestedPayload(
        Long settlementId,
        Long payeeId,
        BigDecimal totalGrossAmount,
        BigDecimal totalNetAmount,
        BigDecimal totalFeeAmount
) implements KafkaPayload {}
