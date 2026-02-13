package com.back.market.event;

import com.back.common.event.EventName;

public record UserCreatedEvent(
        Long id,
        String name,
        String email,
        String address,
        String phone
) implements EventName {
}
