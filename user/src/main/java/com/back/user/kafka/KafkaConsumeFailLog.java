package com.back.user.kafka;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "kafka_consume_fail_log",
        indexes = {
                @Index(name = "idx_kcfl_event_id", columnList = "eventId"),
                @Index(name = "idx_kcfl_logged_at", columnList = "loggedAt"),
                @Index(name = "idx_kcfl_consume_pos", columnList = "consumerGroupId,topic,partition,offset")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class KafkaConsumeFailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== Envelope 정보 (가능하면 세팅) =====
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "event_type", length = 200)
    private String eventType;

    // ===== Consumer 메타 =====
    @Column(name = "consumer_group_id", nullable = false, length = 200)
    private String consumerGroupId;

    @Column(name = "topic", nullable = false, length = 250)
    private String topic;

    @Column(name = "partition", nullable = false)
    private Integer partition;

    @Column(name = "offset", nullable = false)
    private Long offset;

    // ===== 실패 정보 =====
    @Column(name = "retry_exhausted", nullable = false)
    private boolean retryExhausted;

    @Column(name = "error_class", nullable = false, length = 300)
    private String errorClass;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Lob
    @Column(name = "payload_json")
    private String payloadJson;

    @Lob
    @Column(name = "stacktrace")
    private String stacktrace;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    @PrePersist
    void prePersist() {
        if (loggedAt == null) {
            loggedAt = LocalDateTime.now();
        }
    }
}

