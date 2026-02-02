package com.back.common.user.event;

import java.time.LocalDateTime;

public record UserCreatedEvent(
        Long userId,
        String nickname,
        String email,
        String address,
        String phone,
        String profileImageUrl,
        LocalDateTime createdAt
) {
}
