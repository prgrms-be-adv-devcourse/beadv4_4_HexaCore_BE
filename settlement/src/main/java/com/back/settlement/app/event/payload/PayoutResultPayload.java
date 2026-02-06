package com.back.settlement.app.event.payload;

import com.back.common.event.KafkaPayload;

public record PayoutResultPayload(
        Long settlementId,
        boolean success,
        String failReason
) implements KafkaPayload {}