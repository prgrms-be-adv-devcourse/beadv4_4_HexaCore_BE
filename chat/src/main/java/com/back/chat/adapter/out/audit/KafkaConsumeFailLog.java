package com.back.chat.adapter.out.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "kafka_consume_fail_log",
        indexes = {
                @Index(name = "idx_kcfl_event_id", columnList = "event_id"),
                @Index(name = "idx_kcfl_logged_at", columnList = "logged_at"),
                @Index(name = "idx_kcfl_topic_logged_at", columnList = "topic,logged_at"),
                @Index(name = "idx_kcfl_consume_pos", columnList = "consumer_group_id,topic,topic_partition,topic_offset")
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

    @Column(name = "event_id", nullable = true)
    private UUID eventId;

    @Column(name = "event_type", length = 200, nullable = true)
    private String eventType;

    @Column(name = "consumer_group_id", nullable = false, length = 200)
    private String consumerGroupId;

    @Column(name = "topic", nullable = false, length = 250)
    private String topic;

    @Column(name = "topic_partition", nullable = false)
    private Integer topicPartition;

    @Column(name = "topic_offset", nullable = false)
    private Long topicOffset;

    @Column(name = "record_timestamp_epoch_ms", nullable = true)
    private Long recordTimestampEpochMs;

    // ===== 실패 정보 =====
    @Column(name = "error_class", nullable = false, length = 300)
    private String errorClass;

    @Column(name = "error_message", length = 2000, nullable = true)
    private String errorMessage;

    @Column(name = "payload_json", columnDefinition = "TEXT", nullable = true)
    private String payloadJson;

    @Column(name = "stacktrace", columnDefinition = "TEXT", nullable = true)
    private String stacktrace;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    public static KafkaConsumeFailLog fromCommand(KafkaConsumeFailLogCommand c) {
        return KafkaConsumeFailLog.builder()
                .eventId(c.eventId())
                .eventType(c.eventType())
                .consumerGroupId(c.consumerGroupId())
                .topic(c.topic())
                .topicPartition(c.topicPartition())
                .topicOffset(c.topicOffset())
                .recordTimestampEpochMs(c.recordTimestampEpochMs())
                .errorClass(c.errorClass())
                .errorMessage(c.errorMessage())
                .payloadJson(c.payloadJson())
                .stacktrace(c.stacktrace())
                .loggedAt(c.loggedAt())
                .build();
    }

    @PrePersist
    void prePersist() {
        if (loggedAt == null) {
            loggedAt = LocalDateTime.now();
        }
    }
}
