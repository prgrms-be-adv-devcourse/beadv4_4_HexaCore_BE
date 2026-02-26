package com.back.notification.dto.payload;

import com.back.common.event.KafkaPayload;

public record UserCreatedPayload(
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
