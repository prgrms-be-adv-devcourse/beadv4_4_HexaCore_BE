package com.back.common.user.event;

import com.back.common.event.EventName;

public record FcmTokenChangedEvent(
        Long userId,
        String fcmToken
) implements EventName {
}
