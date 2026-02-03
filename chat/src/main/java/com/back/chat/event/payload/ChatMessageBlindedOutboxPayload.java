package com.back.chat.event.payload;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageBlindedOutboxPayload(
        UUID eventId,
        Long messageId,
        Long roomId,
        Long authorUserId,
        LocalDateTime blindedAt
) {
}
