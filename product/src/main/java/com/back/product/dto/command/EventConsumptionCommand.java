package com.back.product.dto.command;

import lombok.Builder;

@Builder
public record EventConsumptionCommand(
        String eventId,
        String eventType,
        String topic,
        Integer partition,
        Long offset,
        String message
) {
}
