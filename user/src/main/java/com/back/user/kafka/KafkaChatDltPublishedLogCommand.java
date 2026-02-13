package com.back.user.kafka;

import java.time.LocalDateTime;
import java.util.UUID;

public record KafkaChatDltPublishedLogCommand(

        // ===== Envelope =====
        UUID eventId,
        String eventType,

        // ===== Outbox =====
        Long outboxId,
        String source,
        String dltTopic,

        // ===== Publish Result =====
        Integer publishedPartition,
        Long publishedOffset,
        Long recordTimestamp,
        boolean success,

        // ===== Error / Payload =====
        String payloadJson,
        String errorMessage,

        LocalDateTime loggedAt

) {}
