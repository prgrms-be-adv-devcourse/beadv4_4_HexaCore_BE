package com.back.cash.adapter.out.outbox;

import com.back.cash.domain.outbox.PaymentCompletedOutbox;
import com.back.cash.domain.outbox.enums.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class PaymentCompletedOutboxPoller {

    private final PaymentCompletedOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> outboxKafkaTemplate;

    @Value("${custom.outbox.poll-size:100}")
    private int pollSize;

    @Value("${custom.outbox.max-retry:10}")
    private int maxRetry;

    @Value("${custom.outbox.retry-base-delay-seconds:60}")
    private int retryBaseDelaySeconds;

    @Value("${custom.outbox.retry-max-delay-seconds:300}")
    private int retryMaxDelaySeconds;

    public PaymentCompletedOutboxPoller(
            PaymentCompletedOutboxRepository outboxRepository,
            @Qualifier("outboxKafkaTemplate") KafkaTemplate<String, String> outboxKafkaTemplate
    ) {
        this.outboxRepository = outboxRepository;
        this.outboxKafkaTemplate = outboxKafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void poll() {
        List<PaymentCompletedOutbox> pending =
                outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, Limit.of(pollSize));

        List<PaymentCompletedOutbox> retryable =
                outboxRepository.findRetryable(maxRetry, LocalDateTime.now(), Limit.of(pollSize));

        process(pending);
        process(retryable);
    }

    private void process(List<PaymentCompletedOutbox> list) {
        for (PaymentCompletedOutbox outbox : list) {
            try {
                outboxKafkaTemplate.send(outbox.getTopic(), outbox.getPayload()).get();
                outbox.markAsSent();
                log.info("[PAYMENT_COMPLETED_OUTBOX_SENT] 결제 검증 완료 이벤트 발행 eventId={}, topic={}", outbox.getEventId(), outbox.getTopic());
            } catch (Exception e) {
                outbox.markAsFailed(retryBaseDelaySeconds, retryMaxDelaySeconds);
                log.error("[PAYMENT_COMPLETED_OUTBOX_SEND_FAILED] 결제 검증 완료 이벤트 실패 eventId={}, retryCount={}, nextRetryAt={}",
                        outbox.getEventId(), outbox.getRetryCount(), outbox.getNextRetryAt(), e);
            }
        }
    }
}
