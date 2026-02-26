package com.back.user.app.event;

import com.back.common.event.KafkaPayload;

public record FcmTokenChangedPayload(
        Long userId,
        String fcmToken
) implements KafkaPayload {
}
