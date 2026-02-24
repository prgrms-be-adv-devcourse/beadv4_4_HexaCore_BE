package com.back.cash.domain.outbox;

import com.back.cash.domain.outbox.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCompletedOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;

    private String topic;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    private int retryCount;

    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private LocalDateTime nextRetryAt;

    public static PaymentCompletedOutbox createPending(String eventId, String topic, String payload) {
        PaymentCompletedOutbox outbox = new PaymentCompletedOutbox();
        outbox.eventId = eventId;
        outbox.topic = topic;
        outbox.payload = payload;
        outbox.status = OutboxStatus.PENDING;
        outbox.retryCount = 0;
        outbox.createdAt = LocalDateTime.now();
        return outbox;
    }

    public void markAsSent() {
        this.status = OutboxStatus.SENT;
        this.processedAt = LocalDateTime.now();
    }

    // 지수 백오프: 1번 실패 → base초, 2번 → base*2초, 3번 → base*4초 (maxDelay 상한선)
    public void markAsFailed(int baseDelaySeconds, int maxDelaySeconds) {
        this.retryCount++;
        this.status = OutboxStatus.FAILED;
        long delaySeconds = Math.min(
                (long) Math.pow(2, retryCount - 1) * baseDelaySeconds,
                maxDelaySeconds
        );
        this.nextRetryAt = LocalDateTime.now().plusSeconds(delaySeconds);
    }
}
