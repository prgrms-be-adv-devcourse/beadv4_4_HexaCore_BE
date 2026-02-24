package com.back.user.app.event;

import com.back.common.event.KafkaPayload;
import lombok.Builder;

@Builder
public record UserUpdatedPayload(
        Long id,
        String nickname,
        String name,
        String email,
        String address,
        String phone
) implements KafkaPayload
{}
