package com.back.chat.adapter.out.outbox;

import com.back.chat.adapter.out.metrics.ChatMetrics;
import com.back.chat.domain.event.ChatEventType;
import com.back.chat.domain.event.ChatOutboxSavedEvent;
import com.back.common.chat.ChatMessageBlindedKafkaEvent;
import com.back.common.code.FailureCode;
import com.back.common.event.Envelope;
import com.back.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

import static com.back.chat.adapter.out.outbox.OutboxUtil.safeMsg;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisher {

    private final KafkaTemplate<String, Envelope<ChatMessageBlindedKafkaEvent>> kafkaTemplate;

    private final ChatOutboxRepository outboxRepository;
    private final OutboxStatusUpdater statusUpdater;

    private final JsonMapper jsonMapper;

    private final ChatMetrics metrics;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publish(ChatOutboxSavedEvent event){
        ChatOutbox outbox = outboxRepository.findById(event.outboxId()).orElseThrow(()->new BadRequestException(FailureCode.CHAT_OUTBOX_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        int claimed = outboxRepository.claimOneById(outbox.getId(), now);
        if (claimed == 0) return;

        ChatMessageBlindedKafkaEvent payload = jsonMapper.readValue(outbox.getPayload(), ChatMessageBlindedKafkaEvent.class);

        Envelope<ChatMessageBlindedKafkaEvent> envelope = Envelope.of(outbox.getEventId().toString(), ChatEventType.MESSAGE_BLINDED.toString(), payload);

        kafkaTemplate.send(OutboxPollingProperties.CHAT_BLIND_REQUESTED_TOPIC, envelope)
                .whenComplete((res, ex) -> {
                    LocalDateTime now2 = LocalDateTime.now();

                    if (ex == null) {
                        statusUpdater.markSent(outbox.getId(), now2);
                        return;
                    }

                    log.warn("[OUTBOX] publish failed outboxId={}, eventId={}, err={}",
                            outbox.getId(), outbox.getEventId(), safeMsg(ex));

                    metrics.incOutboxPublishFail(outbox.getEventType().name(), ex.getClass().getSimpleName());

                    statusUpdater.markFailed(
                            outbox.getId(),
                            safeMsg(ex),
                            now2,
                            OutboxPollingProperties.RETRY_BASE_DELAY_SECONDS
                    );
                });
    }
}
