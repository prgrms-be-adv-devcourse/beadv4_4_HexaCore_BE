package com.back.chat.adapter.out.outbox;

import com.back.chat.domain.event.ChatEventType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;

import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.GenerationType.*;

@Entity
@Table(
        name = "chat_outbox"
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatOutbox {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, updatable = false, unique = true)
    private UUID eventId;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private ChatEventType eventType;

    @Column(columnDefinition = "jsonb", nullable = false)
    @ColumnTransformer(write = "?::jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "dead_at")
    private LocalDateTime deadAt;

    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    private ChatOutbox(
            UUID eventId,
            String aggregateType,
            Long aggregateId,
            ChatEventType eventType,
            String payload,
            LocalDateTime createdAt
    ) {
        this.eventId = eventId;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.nextAttemptAt = createdAt;
        this.status = OutboxStatus.PENDING;
        this.createdAt = createdAt;
        this.retryCount = 0;
    }

    public static ChatOutbox pending(
            UUID eventId,
            String aggregateType,
            Long aggregateId,
            ChatEventType eventType,
            String payload,
            LocalDateTime createdAt
    ) {
        return new ChatOutbox(
                eventId,
                aggregateType,
                aggregateId,
                eventType,
                payload,
                createdAt
        );
    }
}
