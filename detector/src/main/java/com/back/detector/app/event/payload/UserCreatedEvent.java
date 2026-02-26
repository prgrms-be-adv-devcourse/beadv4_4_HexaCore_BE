package com.back.detector.app.event.payload;

import com.back.common.event.KafkaPayload;

public record UserCreatedEvent(
        Long id,
        String nickname,
        String name,
        String email,
        String address,
        String phone,
        String profileImageUrl,
        String ipAddress
) implements KafkaPayload {
}
