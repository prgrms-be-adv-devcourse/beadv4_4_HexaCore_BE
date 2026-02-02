package com.back.common.user.event;

public record UserCreatedEvent(
        Long id,
        String nickname,
        String email,
        String address,
        String phone,
        String profileImageUrl
) {
}
