package com.back.common.chat;


import java.time.LocalDateTime;

public record ChatMessageBlindedKafkaEvent(
        String eventId,
        Long authorUserId,
        LocalDateTime createdAt
) {
}
