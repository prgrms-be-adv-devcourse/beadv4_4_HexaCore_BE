package com.back.settlement.adapter.out;

import com.back.settlement.domain.outbox.SettlementOutboxEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class SettlementOutboxEventSender {
    private final SettlementOutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> outboxKafkaTemplate;
    private final int retryDelaySeconds;

    public SettlementOutboxEventSender(SettlementOutboxRepository outboxRepository, @Qualifier("outboxKafkaTemplate") KafkaTemplate<String, String> outboxKafkaTemplate, @Value("${settlement.outbox.polling.retry-delay-seconds}") int retryDelaySeconds) {
        this.outboxRepository = outboxRepository;
        this.outboxKafkaTemplate = outboxKafkaTemplate;
        this.retryDelaySeconds = retryDelaySeconds;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(Long outboxId) {
        SettlementOutboxEvent outbox = outboxRepository.findById(outboxId).orElse(null);
        if (outbox == null) {
            log.warn("[OUTBOX] 대상 이벤트를 찾을 수 없습니다. outboxId={}", outboxId);
            return;
        }
        try {
            outboxKafkaTemplate.send(outbox.getTopic(), outbox.getPayload()).get();
            outbox.markAsSent();
            log.debug("[OUTBOX] 발행 성공. outboxId={}", outbox.getId());
        } catch (Exception ex) {
            outbox.markAsFailed(retryDelaySeconds);
            log.warn("[OUTBOX] 발행 실패. outboxId={}, retryCount={}, error={}", outbox.getId(), outbox.getRetryCount(), ex.getMessage());
        }
    }
}
