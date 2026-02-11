package com.back.common.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventHeader(
        String eventId,
        String eventType,
        LocalDateTime occurrenceAt
) {

    public static EventHeader create(String eventType) {
        return new EventHeader(
                UUID.randomUUID().toString(),
                eventType,
                LocalDateTime.now()
        );
    }

    public static EventHeader create(String eventId, String eventType) {
        return new EventHeader(
                eventId,
                eventType,
                LocalDateTime.now()
        );
    }
}
