package com.back.common.user.event;

import com.back.common.event.EventName;

public record fcmTokenChangedEvent (
        Long userId,
        String fcmToken
) implements EventName {
}
