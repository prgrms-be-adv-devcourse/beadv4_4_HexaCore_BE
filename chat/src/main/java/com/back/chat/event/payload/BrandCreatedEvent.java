package com.back.chat.event.payload;

import com.back.common.event.KafkaPayload;

public record BrandCreatedEvent(
        Long brandId
) implements KafkaPayload {
}
