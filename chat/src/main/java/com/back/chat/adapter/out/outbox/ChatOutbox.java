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
                @Index(name = "idx_chat_outbox_status_id", columnList = "status, id")
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

    private void requirePending() {
        if (this.status != OutboxStatus.PENDING) {
            throw new IllegalStateException("Outbox is not PENDING: " + this.status);
        }
    }

    public void markSent() {
        requirePending();
        this.status = OutboxStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage) {
        requirePending();
        this.status = OutboxStatus.FAILED;
        this.retryCount++;
        this.lastError = truncate(errorMessage, 1000);
    }
    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
