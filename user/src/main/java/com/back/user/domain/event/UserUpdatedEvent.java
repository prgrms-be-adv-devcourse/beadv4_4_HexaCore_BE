package com.back.user.domain.event;

import com.back.common.event.EventName;
import lombok.Builder;

@Builder
public record UserUpdatedEvent(
        Long id,
        String nickname,
        String name,
        String email,
        String address,
        String phone
) implements EventName {
}
