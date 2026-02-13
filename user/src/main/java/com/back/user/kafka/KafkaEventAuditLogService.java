package com.back.user.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KafkaEventAuditLogService {

    private final KafkaConsumeFailLogRepository kafkaConsumeFailLogRepository;
    private final KafkaChatDltPublishedLogRepository kafkaChatDltPublishedLogRepository;

    @Transactional
    public void saveLog(KafkaConsumeFailLogCommand dto){
        KafkaConsumeFailLog log = KafkaConsumeFailLog.builder()
                .eventId(dto.eventId())
                .eventType(dto.eventType())
                .consumerGroupId(dto.consumerGroupId())
                .topic(dto.topic())
                .partition(dto.partition())
                .offset(dto.offset())
                .recordTimestamp(dto.recordTimestamp())
                .errorClass(dto.errorClass())
                .errorMessage(dto.errorMessage())
                .payloadJson(dto.payload())
                .stacktrace(dto.stacktrace())
                .loggedAt(dto.loggedAt())
                .build();

        kafkaConsumeFailLogRepository.save(log);
    }

    @Transactional
    public void saveLog(KafkaChatDltPublishedLogCommand dto){
        KafkaChatDltPublishedLog log = KafkaChatDltPublishedLog.builder()
                .eventId(dto.eventId())
                .eventType(dto.eventType())
                .outboxId(dto.outboxId())
                .source(dto.source())
                .dltTopic(dto.dltTopic())
                .publishedPartition(dto.publishedPartition())
                .publishedOffset(dto.publishedOffset())
                .recordTimestamp(dto.recordTimestamp())
                .success(dto.success())
                .payloadJson(dto.payloadJson())
                .errorMessage(dto.errorMessage())
                .build();

        kafkaChatDltPublishedLogRepository.save(log);
    }
}
