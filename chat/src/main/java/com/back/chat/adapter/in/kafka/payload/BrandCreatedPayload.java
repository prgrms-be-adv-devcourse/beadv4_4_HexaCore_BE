package com.back.chat.adapter.in.kafka.payload;

import com.back.common.event.KafkaPayload;

public record BrandCreatedPayload(
        Long brandId
) implements KafkaPayload {
}
