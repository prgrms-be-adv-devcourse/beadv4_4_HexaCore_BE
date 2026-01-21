package com.back.chat.event.payload;

import java.time.LocalDateTime;

public record ChatMessageBlindedPayload(
        Long roomId,
        Long chatMessageId,
        LocalDateTime blindedAt
) {
}
