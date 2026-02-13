package com.back.user.adapter.in.chat;

import com.back.common.chat.ChatDeadLetterPayload;
import com.back.common.event.Envelope;
import com.back.user.kafka.KafkaChatDltPublishedLogCommand;
import com.back.user.kafka.KafkaEventAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageBlindedDltListener {

    private final JsonMapper jsonMapper;

    private final KafkaEventAuditLogService kafkaEventAuditLogService;

    @KafkaListener(
            topics = "${custom.kafka.topic.chat-blind-dlt-requested:chat.blind.dlt.requested}",
            containerFactory = "stringKafkaListenerContainerFactory"
    )
    public void onDlt(String json, Acknowledgment ack, ConsumerRecord<String, String> record) {

        try {
            Envelope<ChatDeadLetterPayload> envelope = jsonMapper.readValue(
                    json, new TypeReference<Envelope<ChatDeadLetterPayload>>() {});

            ChatDeadLetterPayload payload = envelope.payload();

            UUID eventUuid = safeUuid(envelope.header().eventId());

            KafkaChatDltPublishedLogCommand command = new KafkaChatDltPublishedLogCommand(
                    eventUuid,
                    envelope.header().eventType(),
                    payload.outboxId(),
                    payload.source(),
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.timestamp(),
                    true,
                    payload.originalPayload(),
                    payload.lastError(),
                    LocalDateTime.now()
            );

            kafkaEventAuditLogService.saveLog(command);

        } catch (Exception e) {
            // DLT는 재시도 루프 방지: 로그만 남기고 삼킴
            log.error("[USER][KAFKA][DLT] failed to audit. topic={}, partition={}, offset={}, err={}",
                    record.topic(), record.partition(), record.offset(), e.toString(), e);

        } finally {
            ack.acknowledge();
        }
    }

    private static UUID safeUuid(String s) {
        if (s == null || s.isBlank()) return null;
        try { return UUID.fromString(s.trim()); }
        catch (IllegalArgumentException e) { return null; }
    }
}
