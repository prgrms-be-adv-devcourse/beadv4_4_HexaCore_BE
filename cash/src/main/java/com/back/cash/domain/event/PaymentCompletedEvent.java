package com.back.cash.domain.event;

import com.back.common.dto.cash.enums.RelType;
import com.back.common.event.EventName;

import java.math.BigDecimal;

public record PaymentCompletedEvent(
        RelType relType,
        Long relId,
        BigDecimal totalAmount
) implements EventName {
}
