package com.back.chat.adapter.out;

import com.back.chat.adapter.out.outbox.ChatOutbox;
import com.back.chat.adapter.out.outbox.ChatOutboxRepository;
import com.back.chat.event.ChatEventType;
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


    @Value("${chat.kafka.topics.message-blinded:chat.message.blinded.v1}")
    private String messageBlindedTopic;

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

    @Scheduled(fixedDelayString = "${chat.outbox.relay.fixed-delay-ms:1000}")
    @Transactional
    public void relay() {
        LocalDateTime now = LocalDateTime.now();

        List<ChatOutbox> batch = chatOutboxRepository.lockReadyBatch(now, batchSize, maxRetry);
        if (batch.isEmpty()) return;

        int success = 0;
        int failed = 0;

        for (ChatOutbox outbox : batch) {
            try {
                String topic = resolveTopic(outbox.getEventType());
                String key = outbox.getEventId().toString();

                kafkaTemplate.send(topic, key, outbox.getPayload())
                        .get(sendTimeoutMs, TimeUnit.MILLISECONDS);

                outbox.markSent(now);
                success++;

            } catch (Exception e) {
                failed++;

                outbox.markFailed(
                        e.getMessage(),
                        now,
                        retryBaseDelaySeconds,
                        retryMaxDelaySeconds
                );

                log.warn("[OUTBOX][FAILED] id={}, eventId={}, type={}, retryCount={}, nextAttemptAt={}, errorType={}, error={}",
                        outbox.getId(), outbox.getEventId(), outbox.getEventType(),
                        outbox.getRetryCount(), outbox.getNextAttemptAt(),
                        e.getClass().getSimpleName(), safeMsg(e)
                );

                if (e instanceof InterruptedException || e.getCause() instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("[OUTBOX][BATCH] size={}, success={}, failed={}", batch.size(), success, failed);
    }

    private String resolveTopic(ChatEventType eventType) {
        return switch (eventType) {
            case MESSAGE_BLINDED -> messageBlindedTopic;
            default -> throw new IllegalArgumentException("Unsupported event type: " + eventType);
        };
    }

    private String safeMsg(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return e.getClass().getSimpleName();
        return msg.length() <= 200 ? msg : msg.substring(0, 200);
    }
}
