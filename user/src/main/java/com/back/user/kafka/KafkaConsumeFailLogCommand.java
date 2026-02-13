package com.back.user.kafka;

import java.time.LocalDateTime;
import java.util.UUID;

public record KafkaConsumeFailLogCommand(
        UUID eventId,
        String eventType,

        String consumerGroupId,
        String topic,
        Integer partition,
        Long offset,
        Long recordTimestamp,

        String payload,
        String errorClass,
        String errorMessage,
        String stacktrace,

        LocalDateTime loggedAt
) {}
