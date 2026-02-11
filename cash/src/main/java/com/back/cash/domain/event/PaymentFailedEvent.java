package com.back.cash.domain.event;

import com.back.common.dto.cash.enums.RelType;
import com.back.common.event.EventName;

public record PaymentFailedEvent(
        RelType relType,
        Long relId
) implements EventName {
}
