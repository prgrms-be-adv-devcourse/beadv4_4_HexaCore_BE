package com.back.cash.app.event;

import com.back.common.event.KafkaPayload;

public record PayoutFailedPayload (
        Long settlementId,
        String reason
) implements KafkaPayload {}
