package com.back.user.domain.event;

import com.back.common.event.EventName;

public record UserCreatedEvent(
        Long id,
        String nickname,
        String name,
        String email,
        String address,
        String phone,
        String profileImageUrl,
        String ipAddress
) implements EventName {
}
