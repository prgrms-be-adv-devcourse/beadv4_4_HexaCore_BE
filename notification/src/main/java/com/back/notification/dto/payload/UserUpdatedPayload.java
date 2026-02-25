package com.back.notification.dto.payload;

import com.back.common.event.KafkaPayload;

public record UserUpdatedPayload(
        Long id,
        String nickname,
        String name
) implements KafkaPayload {
}
