package com.back.settlement.app.event.payload;

import com.back.common.event.KafkaPayload;

public record PayoutFailedPayload(
        Long settlementId,
        String reason
) implements KafkaPayload {}
