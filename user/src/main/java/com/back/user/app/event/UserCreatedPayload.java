package com.back.user.app.event;

import com.back.common.event.KafkaPayload;

public record UserCreatedPayload(
        Long id,
        String nickname,
        String name,
        String email,
        String address,
        String phone,
        String profileImageUrl
) implements KafkaPayload
{}
