package com.back.chat.adapter.out.outbox;

import com.back.common.chat.ChatDeadLetterPayload;
import com.back.common.chat.ChatMessageBlindedKafkaEvent;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Component
@RequiredArgsConstructor
public class ChatOutboxRelay {

    private final ChatOutboxTxService chatOutboxTxService;
    private final OutboxStatusUpdater outboxStatusUpdater;

    private final KafkaTemplate<String,ChatMessageBlindedKafkaEvent> blindedEventKafkaTemplate;
    private final KafkaTemplate<String, ChatDeadLetterPayload> deadLetterKafkaTemplate;

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

    @Scheduled(fixedDelayString = "${chat.outbox.relay.fixed-delay-ms:2000}")
    public void tick() {
        relayOnce();
    }

    @Scheduled(fixedDelayString = "${chat.outbox.relay.recover-delay-ms:60000}")
    public void recoveryTick() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusSeconds(processingTimeoutSeconds);

        int recovered = chatOutboxTxService.recoverStuckProcessing(cutoff, now);
        if (recovered > 0) {
            log.warn("[OUTBOX][RECOVER] recovered={} cutoff={}", recovered, cutoff);
        }
    }


    public void relayOnce() {
        LocalDateTime now = LocalDateTime.now();

        List<ChatOutbox> claimed = chatOutboxTxService.claim(now, batchSize, maxRetry);
        if (claimed.isEmpty()) return;

        final AtomicInteger success = new AtomicInteger();
        final AtomicInteger failed = new AtomicInteger();
        final AtomicInteger dead = new AtomicInteger();

        log.info("[OUTBOX][BATCH_START] claimed={}", claimed.size());

        for (ChatOutbox outbox : claimed) {

            final Long outboxId = outbox.getId();
            final String eventId = outbox.getEventId().toString(); // key로 사용
            final String eventTypeName = outbox.getEventType().name();
            final String payloadJson = outbox.getPayload();
            final int currentRetry = outbox.getRetryCount(); // claim 시점의 값

            final int nextRetry = outbox.getRetryCount();
            // 이번 실패로 dlt 보낼지.
            final boolean willGoDltOnThisFailure = nextRetry >= maxRetry;

            final ChatMessageBlindedKafkaEvent payload;
            try {
                payload = objectMapper.readValue(outbox.getPayload(), ChatMessageBlindedKafkaEvent.class);

            } catch (Exception parseEx) {
                // payload 실패 -> 즉시 DLT 동기 발행
                String err = safeMsg(parseEx);

                boolean dltOk = sendDltSync(outboxId, eventTypeName, eventId, nextRetry, err, payloadJson);

                if (dltOk) {
                    dead.incrementAndGet();
                    outboxStatusUpdater.markDead(outboxId, "sent to dlt: parse failed", LocalDateTime.now());
                }

                log.error("[OUTBOX][PARSE_FAILED] id={}, eventId={}, type={}, retryCount={}, dltOk={}, error={}",
                        outboxId, eventId, eventTypeName, currentRetry, dltOk, err);
                continue;
            }

            // 정상 토픽 비동기 발행
            blindedEventKafkaTemplate
                    .send(messageBlindedTopic, eventId, payload)
                    .orTimeout(sendTimeoutMs, TimeUnit.MILLISECONDS)
                    .whenComplete((sendResult, ex) -> {
                        final LocalDateTime cbNow = LocalDateTime.now();

                        if (ex == null) {
                            success.incrementAndGet();
                            outboxStatusUpdater.markSent(outboxId, cbNow);
                            return;
                        }

                        failed.incrementAndGet();
                        String err = safeMsg(ex);

                        outboxStatusUpdater.markFailed(outboxId, err, cbNow,
                                retryBaseDelaySeconds, retryMaxDelaySeconds);

                        if (willGoDltOnThisFailure) {
                            // DLT 동기 발행
                            boolean dltOk = sendDltSync(outboxId, eventTypeName, eventId, nextRetry, err, payloadJson);
                            if (dltOk) {
                                dead.incrementAndGet();
                                outboxStatusUpdater.markDead(outboxId, "sent to dlt after max retry", cbNow);
                                log.error("[OUTBOX][DLT] id={}, eventId={}, type={}, retryCount(next)={}",
                                        outboxId, eventId, eventTypeName, nextRetry);
                            } else {
                                log.error("[OUTBOX][DLT_FAILED] id={}, eventId={}, type={}, retryCount(next)={}, error={}",
                                        outboxId, eventId, eventTypeName, nextRetry, err);
                            }
                        } else {
                            log.warn("[OUTBOX][FAILED] id={}, eventId={}, type={}, retryCount(before)={}, nextRetry={}, error={}",
                                    outboxId, eventId, eventTypeName, currentRetry, nextRetry, err);
                        }
                    });
        }

        log.info("[OUTBOX][BATCH_DISPATCHED] claimed={}, success~={}, failed~={}, dead~={}",
                claimed.size(), success.get(), failed.get(), dead.get());
    }

    private boolean sendDltSync(Long outboxId,
                                String eventTypeName,
                                String eventId,
                                int retryCount,
                                String err,
                                String payloadJson) {

        ChatDeadLetterPayload dlt = new ChatDeadLetterPayload(
                "chat-outbox-relay",
                eventTypeName,
                eventId,
                outboxId,
                retryCount,
                err,
                LocalDateTime.now(),
                payloadJson
        );

        try {
            deadLetterKafkaTemplate
                    .send(messageBlindedDltTopic, eventId, dlt)
                    .get(sendTimeoutMs, TimeUnit.MILLISECONDS);
            return true;
        } catch (Exception e) {
            log.error("[OUTBOX][DLT_SEND_EXCEPTION] id={}, eventId={}, type={}, error={}",
                    outboxId, eventId, eventTypeName, safeMsg(e));
            if (e instanceof InterruptedException || e.getCause() instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    private String safeMsg(Throwable t) {
        if (t == null) return "Unknown";
        String msg = t.getMessage();
        if (msg == null) return t.getClass().getSimpleName();
        return msg.length() <= 200 ? msg : msg.substring(0, 200);
    }
}
