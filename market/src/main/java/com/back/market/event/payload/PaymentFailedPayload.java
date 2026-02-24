package com.back.market.event.payload;

import com.back.common.dto.cash.enums.RelType;
import com.back.common.event.KafkaPayload;

public record PaymentFailedPayload(
        RelType relType,
        Long relId,
        String failReason
) implements KafkaPayload {
}
