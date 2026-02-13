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
    public void saveLog(KafkaConsumeFailLogCommand command){
        KafkaConsumeFailLog log = KafkaConsumeFailLog.builder()
                .eventId(command.eventId())
                .eventType(command.eventType())
                .consumerGroupId(command.consumerGroupId())
                .topic(command.topic())
                .partition(command.partition())
                .offset(command.offset())
                .recordTimestamp(command.recordTimestamp())
                .errorClass(command.errorClass())
                .errorMessage(command.errorMessage())
                .payloadJson(command.payload())
                .stacktrace(command.stacktrace())
                .loggedAt(command.loggedAt())
                .build();

        kafkaConsumeFailLogRepository.save(log);
    }

    @Transactional
    public void saveLog(KafkaChatDltPublishedLogCommand command){
        KafkaChatDltPublishedLog log = KafkaChatDltPublishedLog.builder()
                .eventId(command.eventId())
                .eventType(command.eventType())
                .outboxId(command.outboxId())
                .source(command.source())
                .dltTopic(command.dltTopic())
                .publishedPartition(command.publishedPartition())
                .publishedOffset(command.publishedOffset())
                .recordTimestamp(command.recordTimestamp())
                .success(command.success())
                .payloadJson(command.payloadJson())
                .errorMessage(command.errorMessage())
                .loggedAt(command.loggedAt())
                .build();

        kafkaChatDltPublishedLogRepository.save(log);
    }
}
