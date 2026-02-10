package com.back.cash.app.event;

import com.back.common.dto.cash.enums.RelType;
import com.back.common.event.KafkaPayload;

import java.math.BigDecimal;

public record PaymentCompletedPayload(
        RelType relType,
        Long relId,
        BigDecimal totalAmount
) implements KafkaPayload {}
