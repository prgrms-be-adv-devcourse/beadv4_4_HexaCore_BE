package com.back.common.chat;

import com.back.common.event.EventName;

import java.time.LocalDateTime;

public record ChatMessageBlindedKafkaEvent(
        String eventId,
        Long authorUserId,
        LocalDateTime createdAt
) implements EventName {
}
