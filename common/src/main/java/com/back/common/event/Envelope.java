package com.back.common.event;

public record Envelope<T extends KafkaPayload>(
        EventHeader header,
        T payload
) implements EventName {

    public static <T extends KafkaPayload> Envelope<T> of(String eventType, T payload) {
        return new Envelope<>(EventHeader.create(eventType), payload);
    }

    public static <T extends KafkaPayload> Envelope<T> of(String eventId, String eventType, T payload) {
        return new Envelope<>(EventHeader.create(eventId, eventType), payload);
    }
}
