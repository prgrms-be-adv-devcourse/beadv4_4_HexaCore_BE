package com.back.chat.adapter.out.outbox;

import com.back.chat.event.ChatEventType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.GenerationType.*;

@Entity
@Table(
        name = "chat_outbox",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_chat_outbox_event_id", columnNames="event_id")
        },
        indexes = {
                @Index(
                        name = "idx_chat_outbox_status_next_attempt_id",
                        columnList = "status, next_attempt_at, id"
                )
        }
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

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "next_attempt_at", nullable = false)
    private LocalDateTime nextAttemptAt;

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

    private void requirePendingOrFailed() {
        if (this.status != OutboxStatus.PENDING && this.status != OutboxStatus.FAILED) {
            throw new IllegalStateException("Outbox status must be PENDING or FAILED, but was " + this.status);
        }
    }

    public void markSent(LocalDateTime sentAt) {
        requirePendingOrFailed();
        this.status = OutboxStatus.SENT;
        this.sentAt = sentAt;
    }

    public void markFailed(String errorMessage, LocalDateTime now, int baseDelaySeconds, int maxDelaySeconds) {
        requirePendingOrFailed();
        this.status = OutboxStatus.FAILED;
        this.retryCount++;
        this.lastError = truncate(errorMessage, 1000);

        long delay = (long) baseDelaySeconds << Math.max(0, this.retryCount - 1);
        delay = Math.min(delay, maxDelaySeconds);

        this.nextAttemptAt = now.plusSeconds(delay);
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
