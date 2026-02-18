package com.back.chat.adapter.out.redis.payload;

import java.time.LocalDateTime;

public record ChatMessageBlindedPayload(
        Long roomId,
        Long chatMessageId,
        LocalDateTime blindedAt
) {
}
