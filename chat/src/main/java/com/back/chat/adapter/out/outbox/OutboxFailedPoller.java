package com.back.chat.adapter.out.outbox;

import com.back.chat.adapter.out.metrics.ChatMetrics;
import com.back.chat.domain.event.ChatEventType;
import com.back.common.chat.ChatDeadLetterPayload;
import com.back.common.chat.ChatMessageBlindedKafkaEvent;
import com.back.common.event.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.back.chat.adapter.out.outbox.OutboxUtil.safeMsg;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxFailedPoller {

    private final KafkaTemplate<String, Envelope<ChatMessageBlindedKafkaEvent>> kafkaTemplate;
    private final KafkaTemplate<String, Envelope<ChatDeadLetterPayload>> dltKafkaTemplate;

    private final ChatOutboxRepository outboxRepository;
    private final OutboxStatusUpdater statusUpdater;

    private final JsonMapper jsonMapper;

    private final ChatMetrics metrics;

    private final AtomicLong lastBacklogUpdateEpochMs = new AtomicLong(0);

    @Scheduled(fixedDelay = OutboxPollingProperties.failedPollIntervalMs)
    public void tickFailedOnly() {
        LocalDateTime now = LocalDateTime.now();

        // 30초에 한번 FAILED 상태의 대기 Outbox 개수 조회 및 게이지 갱신.
        updateBacklogIfNeeded();

        // 1) FAILED 중 재시도 시간 도달한 것만 선점
        List<Long> claimedIds =
                outboxRepository.claimFailedBatch(now, OutboxPollingProperties.BATCH_SIZE);
        if (claimedIds.isEmpty()) return;

        // 2) 선점된 것만 조회
        List<ChatOutbox> outboxes =
                outboxRepository.findByIdInOrderByNextAttemptAtAscIdAsc(claimedIds.toArray(new Long[0]));

        for (ChatOutbox outbox : outboxes) {
            ChatMessageBlindedKafkaEvent payload = jsonMapper.readValue(outbox.getPayload(), ChatMessageBlindedKafkaEvent.class);

            Envelope<ChatMessageBlindedKafkaEvent> envelope = Envelope.of(outbox.getEventId().toString(), ChatEventType.MESSAGE_BLINDED.toString(), payload);

            kafkaTemplate
                    .send(
                            OutboxPollingProperties.CHAT_BLIND_REQUESTED_TOPIC, envelope
                    )
                    .whenComplete((res, ex) -> {
                        LocalDateTime now2 = LocalDateTime.now();

                        if (ex == null) {
                            statusUpdater.markSent(outbox.getId(), now2);
                            return;
                        }

                        log.warn("[OUTBOX] retry publish failed outboxId={}, eventId={}, err={}",
                                outbox.getId(), outbox.getEventId(), safeMsg(ex));

                        String resultStatus = statusUpdater.markFailedOrDead(
                                outbox.getId(),
                                safeMsg(ex),
                                now2
                        );

                        if (resultStatus == null) {
                            log.info("[OUTBOX] skip: already handled outboxId={}, eventId={}", outbox.getId(), outbox.getEventId());
                            return;
                        }

                        if (OutboxStatus.DEAD.name().equals(resultStatus)) {
                            ChatDeadLetterPayload dltPayload = new ChatDeadLetterPayload(
                                    "chat-service",
                                    outbox.getEventType().toString(),
                                    outbox.getEventId().toString(),
                                    outbox.getId(),
                                    outbox.getRetryCount(),
                                    safeMsg(ex),
                                    LocalDateTime.now(),
                                    outbox.getPayload()
                            );

                            Envelope<ChatDeadLetterPayload> dltEnvelope = Envelope.of(outbox.getEventId().toString(), ChatEventType.MESSAGE_BLINDED.toString(), dltPayload);

                            dltKafkaTemplate.send(
                                    OutboxPollingProperties.CHAT_BLIND_DLT_REQUESTED_TOPIC, dltEnvelope
                            ).whenComplete((dltRes, dltEx) -> {
                                if (dltEx != null) {
                                    log.warn("[OUTBOX][DLT] send failed outboxId={}, eventId={}, err={}",
                                            outbox.getId(), outbox.getEventId(), safeMsg(dltEx));
                                }
                            });
                        }
                    });
        }
    }

    private void updateBacklogIfNeeded() {
        long nowMs = System.currentTimeMillis();
        long prev = lastBacklogUpdateEpochMs.get();
        if (nowMs - prev < 30_000) return;

        if (!lastBacklogUpdateEpochMs.compareAndSet(prev, nowMs)) return;

        metrics.setOutboxRetryBacklog(Math.toIntExact(outboxRepository.countByStatus(OutboxStatus.FAILED)));
    }
}
