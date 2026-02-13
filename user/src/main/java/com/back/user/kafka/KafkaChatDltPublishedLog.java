package com.back.user.kafka;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "kafka_chat_dlt_published_log",
        indexes = {
                @Index(name = "idx_kcdpl_event_id", columnList = "eventId"),
                @Index(name = "idx_kcdpl_outbox_id", columnList = "outboxId"),
                @Index(name = "idx_kcdpl_logged_at", columnList = "loggedAt")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class KafkaChatDltPublishedLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== Envelope 정보 =====
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "event_type", length = 200)
    private String eventType;

    // ===== Outbox 정보 =====
    @Column(name = "outbox_id")
    private Long outboxId;

    @Column(name = "source", length = 200)
    private String source;

    @Column(name = "dlt_topic", nullable = false, length = 250)
    private String dltTopic;

    // ===== 발행 결과 =====
    @Column(name = "published_partition")
    private Integer publishedPartition;

    @Column(name = "published_offset")
    private Long publishedOffset;

    @Column(name = "success", nullable = false)
    private boolean success;

    // ===== 에러 / payload =====
    @Lob
    @Column(name = "payload_json")
    private String payloadJson;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    @PrePersist
    void prePersist() {
        if (loggedAt == null) {
            loggedAt = LocalDateTime.now();
        }
    }
}

