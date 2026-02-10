package com.back.cash.app.event;

import com.back.common.dto.cash.enums.RelType;
import com.back.common.event.KafkaPayload;

public record PaymentFailPayload(
        RelType relType,
        Long relId
) implements KafkaPayload {}
