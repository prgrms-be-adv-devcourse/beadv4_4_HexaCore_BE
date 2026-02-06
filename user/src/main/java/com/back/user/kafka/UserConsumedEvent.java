package com.back.user.kafka;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "user_consumed_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserConsumedEvent {
    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "consumed_at", nullable = false)
    private LocalDateTime consumedAt;

    private UserConsumedEvent(UUID eventId, String eventType, LocalDateTime consumedAt){
        this.eventId = eventId;
        this.eventType = eventType;
        this.consumedAt = consumedAt;
    }

    public static UserConsumedEvent create(UUID eventId, String eventType, LocalDateTime consumedAt){
        return new UserConsumedEvent(eventId, eventType, consumedAt);
    }
}
