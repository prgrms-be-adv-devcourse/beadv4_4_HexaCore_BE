package com.back.common.chat;

import java.time.LocalDateTime;

public record ChatDeadLetterPayload(
        String source,
        String eventType,
        String eventId,
        Long outboxId,
        int retryCount,
        String lastError,
        LocalDateTime deadAt,
        String originalPayload
) {
}
