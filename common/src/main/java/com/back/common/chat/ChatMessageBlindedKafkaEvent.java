package com.back.common.chat;


import com.back.common.event.KafkaPayload;

import java.time.LocalDateTime;

public record ChatMessageBlindedKafkaEvent(
        String eventId,
        Long authorUserId,
        LocalDateTime createdAt
) implements KafkaPayload {
}
