package com.back.common.market.event;

import com.back.common.event.EventName;

public record UserCreatedEvent(
        Long id,
        String nickname,
        String email,
        String address,
        String phone,
        String profileImageUrl
) implements EventName {
}
