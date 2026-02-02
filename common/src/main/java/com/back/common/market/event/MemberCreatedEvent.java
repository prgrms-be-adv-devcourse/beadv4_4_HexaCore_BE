package com.back.common.market.event;

import com.back.common.event.EventName;

public record MemberCreatedEvent(
        Long id,
        String role,
        String nickname,
        String email,
        String address,
        String phone,
        String profileImageUrl
) implements EventName {
}
