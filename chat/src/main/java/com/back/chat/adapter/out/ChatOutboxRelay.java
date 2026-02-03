package com.back.chat.adapter.out;

import com.back.chat.adapter.out.outbox.ChatOutbox;
import com.back.chat.adapter.out.outbox.ChatOutboxRepository;
import com.back.chat.event.ChatEventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@RequiredArgsConstructor
public class ChatOutboxRelay {

    private final ChatOutboxRepository chatOutboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${chat.kafka.topics.message-blinded:chat.message.blinded.v1}")
    private String messageBlindedTopic;

    @Value("${chat.kafka.topics.message-blinded-dlt:chat.message.blinded.v1.dlt}")
    private String messageBlindedDltTopic;

    @Value("${chat.outbox.relay.batch-size:100}")
    private int batchSize;

    @Value("${chat.outbox.relay.send-timeout-ms:3000}")
    private long sendTimeoutMs;

    @Value("${chat.outbox.relay.max-retry:5}")
    private int maxRetry;

    @Value("${chat.outbox.relay.retry-base-delay-seconds:2}")
    private int retryBaseDelaySeconds;

    @Value("${chat.outbox.relay.retry-max-delay-seconds:60}")
    private int retryMaxDelaySeconds;

    @Value("${chat.outbox.relay.processing-timeout-seconds:300}")
    private long processingTimeoutSeconds;

    @Scheduled(fixedDelayString = "${chat.outbox.relay.fixed-delay-ms:1000}")
    public void tick() {
        // claim → publish
        relayOnce();
    }

    @Scheduled(fixedDelayString = "${chat.outbox.relay.recover-delay-ms:60000}")
    public void recoveryTick() {
        // 오래된 PROCESSING 회수
        recoverStuckProcessing();
    }


    /**
     * Claim은 짧은 트랜잭션으로 끝내고 싶어서 분리
     */
    @Transactional
    public void relayOnce() {
        LocalDateTime now = LocalDateTime.now();

        // READY를 PROCESSING으로 바꾸면서 가져옴 (락 오래 안 잡음)
        List<ChatOutbox> claimed = chatOutboxRepository.claimBatch(now, batchSize, maxRetry);
        if (claimed.isEmpty()) return;

        int success = 0;
        int failed = 0;
        int dead = 0;

        for (ChatOutbox outbox : claimed) {
            try {
                // 정상 토픽 발행
                sendKafka(resolveTopic(outbox.getEventType()), outbox.getEventId().toString(), outbox.getPayload());

                // PROCESSING -> SENT
                outbox.markSent(now);
                success++;

            } catch (Exception e) {
                failed++;

                // PROCESSING -> FAILED (retryCount++, nextAttemptAt 예약)
                outbox.markFailed(safeMsg(e), now, retryBaseDelaySeconds, retryMaxDelaySeconds);

                // maxRetry 도달/초과면 DLT로 이동 시도
                if (outbox.getRetryCount() >= maxRetry) {
                    try {
                        DeadLetterPayload dlt = new DeadLetterPayload(
                                "chat-outbox-relay",
                                outbox.getEventType().name(),
                                outbox.getEventId().toString(),
                                outbox.getId(),
                                outbox.getRetryCount(),
                                outbox.getLastError(),
                                now,
                                outbox.getPayload()
                        );

                        String dltJson = objectMapper.writeValueAsString(dlt);

                        sendKafka(resolveDltTopic(outbox.getEventType()), outbox.getEventId().toString(), dltJson);

                        // DEAD 확정 (더 이상 재시도 X)
                        outbox.markDead("sent to dlt after max retry", now);
                        dead++;

                        log.error("[OUTBOX][DLT] id={}, eventId={}, type={}, retryCount={}",
                                outbox.getId(), outbox.getEventId(), outbox.getEventType(), outbox.getRetryCount());

                    } catch (Exception dltEx) {
                        // DLT 전송 실패면 DEAD로 확정하지 않고 FAILED 유지 (다음 틱에 다시 DLT 시도 가능)
                        log.error("[OUTBOX][DLT_FAILED] id={}, eventId={}, type={}, retryCount={}, error={}",
                                outbox.getId(), outbox.getEventId(), outbox.getEventType(),
                                outbox.getRetryCount(), safeMsg(dltEx));
                    }
                }

                log.warn("[OUTBOX][FAILED] id={}, eventId={}, type={}, retryCount={}, nextAttemptAt={}, error={}",
                        outbox.getId(), outbox.getEventId(), outbox.getEventType(),
                        outbox.getRetryCount(), outbox.getNextAttemptAt(), safeMsg(e));

                if (e instanceof InterruptedException || e.getCause() instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("[OUTBOX][BATCH] claimed={}, success={}, failed={}, dead={}",
                claimed.size(), success, failed, dead);
    }

    /**
       PROCESSING이 너무 오래면 FAILED로 회수해서 nextAttemptAt=now로 재시도 가능하게 만듦
     */
    @Transactional
    public void recoverStuckProcessing() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusSeconds(processingTimeoutSeconds);

        int recovered = chatOutboxRepository.recoverStuckProcessing(cutoff, now);
        if (recovered > 0) {
            log.warn("[OUTBOX][RECOVER] recovered={} cutoff={}", recovered, cutoff);
        }
    }

    private void sendKafka(String topic, String key, String value) throws Exception {
        kafkaTemplate.send(topic, key, value)
                .get(sendTimeoutMs, TimeUnit.MILLISECONDS);
    }

    private String resolveTopic(ChatEventType eventType) {
        return switch (eventType) {
            case MESSAGE_BLINDED -> messageBlindedTopic;
            default -> throw new IllegalArgumentException("Unsupported event type: " + eventType);
        };
    }

    private String resolveDltTopic(ChatEventType eventType) {
        return switch (eventType) {
            case MESSAGE_BLINDED -> messageBlindedDltTopic;
            default -> throw new IllegalArgumentException("Unsupported event type: " + eventType);
        };
    }

    private String safeMsg(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return e.getClass().getSimpleName();
        return msg.length() <= 200 ? msg : msg.substring(0, 200);
    }

    public record DeadLetterPayload(
            String source,
            String eventType,
            String eventId,
            Long outboxId,
            int retryCount,
            String lastError,
            LocalDateTime deadAt,
            String originalPayload
    ) {}
}

