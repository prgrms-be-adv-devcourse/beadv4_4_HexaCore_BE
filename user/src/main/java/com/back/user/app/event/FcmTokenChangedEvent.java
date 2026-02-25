package com.back.user.app.event;

import com.back.common.event.EventName;

public record FcmTokenChangedEvent(
        Long userId,
        String fcmToken
) implements EventName {
}
