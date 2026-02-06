package com.back.settlement.app.event.payload;

import com.back.common.event.KafkaPayload;

import java.math.BigDecimal;

public record PayoutRequestPayload(
        Long settlementId,
        Long payeeId,
        BigDecimal totalGrossAmount,
        BigDecimal totalNetAmount,
        BigDecimal totalFeeAmount
) implements KafkaPayload {}