package com.back.user.domain.event;

public record UserUpdatedEvent(
        Long id,
        String nickname,
        String name,
        String email,
        String address,
        String phone
) {
}
