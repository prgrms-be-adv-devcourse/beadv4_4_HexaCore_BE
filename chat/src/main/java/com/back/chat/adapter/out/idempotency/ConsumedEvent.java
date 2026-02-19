package com.back.chat.adapter.out.idempotency;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "consumed_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsumedEvent {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "consumed_at", nullable = false)
    private LocalDateTime consumedAt;

    private ConsumedEvent(UUID eventId, String eventType, LocalDateTime consumedAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.consumedAt = consumedAt;
    }

    public static ConsumedEvent of(UUID eventId, String eventType, LocalDateTime consumedAt) {
        return new ConsumedEvent(eventId, eventType, consumedAt);
    }
}
