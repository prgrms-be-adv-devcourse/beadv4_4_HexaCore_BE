package com.back.user.adapter.in.chat;

import com.back.common.chat.ChatDeadLetterPayload;
import com.back.common.event.Envelope;
import com.back.user.kafka.KafkaChatDltPublishedLogDto;
import com.back.user.kafka.KafkaEventAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

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
    @Transactional
    public void onDlt(String json, Acknowledgment ack, ConsumerRecord<String, String> record) {

        try {
            Envelope<ChatDeadLetterPayload> envelope = jsonMapper.readValue(
                    json, new TypeReference<Envelope<ChatDeadLetterPayload>>() {});

            ChatDeadLetterPayload payload = envelope.payload();

            UUID eventUuid = safeUuid(envelope.header().eventId());

            KafkaChatDltPublishedLogDto dto = new KafkaChatDltPublishedLogDto(
                    eventUuid,
                    envelope.header().eventType(),
                    payload.outboxId(),
                    payload.source(),
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    true,
                    payload.originalPayload(),
                    payload.lastError(),
                    null
            );

            kafkaEventAuditLogService.saveLog(dto);

        } catch (Exception e) {
            // DLT는 재시도 루프 방지: 로그만 남기고 삼킴
            log.error("[USER][KAFKA][DLT] failed to audit. topic={}, partition={}, offset={}, err={}",
                    record.topic(), record.partition(), record.offset(), e.toString(), e);

        } finally {
            TxAfterCommit.run(() -> { ack.acknowledge(); });
        }
    }

    private static UUID safeUuid(String s) {
        if (s == null || s.isBlank()) return null;
        try { return UUID.fromString(s); }
        catch (IllegalArgumentException e) { return null; }
    }
}
