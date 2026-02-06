package com.back.user.adapter.in;

import com.back.common.chat.ChatDeadLetterPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageBlindedDltListener {

    @KafkaListener(
            topics = "${custom.kafka.topic.chat-blind-dlt-requested:chat.blind.dlt.requested}",
            containerFactory = "chatDeadLetterKafkaListenerContainerFactory"
    )
    public void onDlt(ChatDeadLetterPayload payload,
                      Acknowledgment ack,
                      ConsumerRecord<String, ChatDeadLetterPayload> record) {
        log.error("[USER][DLT] source={}, eventType={}, eventId={}, outboxId={}, retryCount={}, deadAt={}, lastError={}",
                payload.source(), payload.eventType(), payload.eventId(), payload.outboxId(),
                payload.retryCount(), payload.deadAt(), payload.lastError());

        // TODO: DB 적재 / Slack 알림 / 재처리 큐 적재 등
    }
}
