package com.back.chat.adapter.out.audit;

import java.time.LocalDateTime;
import java.util.UUID;


public record KafkaConsumeFailLogCommand(
        UUID eventId,
        String eventType,

        String consumerGroupId,
        String topic,
        Integer topicPartition,
        Long topicOffset,
        Long recordTimestampEpochMs,

        String errorClass,
        String errorMessage,
        String payloadJson,
        String stacktrace,

        LocalDateTime loggedAt
) {
    public static KafkaConsumeFailLogCommand of(
            UUID eventId,
            String eventType,
            String consumerGroupId,
            String topic,
            Integer topicPartition,
            Long topicOffset,
            Long recordTimestampEpochMs,
            Throwable e,
            String payloadJson,
            String stacktrace,
            LocalDateTime loggedAt
    ) {
        return new KafkaConsumeFailLogCommand(
                eventId,
                eventType,
                consumerGroupId,
                topic,
                topicPartition,
                topicOffset,
                recordTimestampEpochMs,
                e.getClass().getName(),
                e.getMessage(),
                payloadJson,
                stacktrace,
                loggedAt
        );
    }
}
