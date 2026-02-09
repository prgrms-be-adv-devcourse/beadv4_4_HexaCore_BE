package com.back.user.kafka;

import jakarta.persistence.*;
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
}
