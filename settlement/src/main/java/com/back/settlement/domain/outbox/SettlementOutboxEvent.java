package com.back.settlement.domain.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "settlement_outbox_event")
public class SettlementOutboxEvent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic", nullable = false, length = 200)
    private String topic;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SettlementOutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "sent_date")
    private LocalDateTime sentDate;

    public static SettlementOutboxEvent createPending(String topic, String payload) {
        LocalDateTime now = LocalDateTime.now();
        return SettlementOutboxEvent.builder()
                .topic(topic)
                .payload(payload)
                .status(SettlementOutboxStatus.PENDING)
                .retryCount(0)
                .createDate(now)
                .updatedAt(now)
                .build();
    }

    public void markAsProcessing() {
        this.status = SettlementOutboxStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsSent() {
        LocalDateTime now = LocalDateTime.now();
        this.status = SettlementOutboxStatus.SENT;
        this.sentDate = now;
        this.nextAttemptAt = null;
        this.updatedAt = now;
    }

    public void markAsFailed(int retryDelaySeconds) {
        LocalDateTime now = LocalDateTime.now();
        this.status = SettlementOutboxStatus.FAILED;
        this.retryCount++;
        this.nextAttemptAt = now.plusSeconds(retryDelaySeconds);
        this.updatedAt = now;
    }

    public void markAsPending() {
        this.status = SettlementOutboxStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
    }
}
