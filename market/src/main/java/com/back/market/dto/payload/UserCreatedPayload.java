package com.back.market.dto.payload;

import com.back.common.event.KafkaPayload;

public record UserCreatedPayload(
        Long id,
        String name,
        String email,
        String address,
        String phone
) implements KafkaPayload {
}
