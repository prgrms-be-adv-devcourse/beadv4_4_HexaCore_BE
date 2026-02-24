package com.back.cash.adapter.in.listener;

import com.back.cash.adapter.out.outbox.PaymentOutboxRepository;
import com.back.cash.app.event.PaymentFailPayload;
import com.back.cash.domain.event.PaymentFailedEvent;
import com.back.cash.domain.outbox.PaymentOutbox;
import com.back.common.event.Envelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFailedOutboxListener {

    private final PaymentOutboxRepository outboxRepository;
    private final JsonMapper jsonMapper;

    @Value("${custom.kafka.topic.cash-payment-failed}")
    private String failedTopic;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(PaymentFailedEvent event) {
        PaymentFailPayload payload = new PaymentFailPayload(event.relType(), event.relId(), event.failReason());

        Envelope<PaymentFailPayload> envelope = Envelope.of(failedTopic, payload);
        String json = jsonMapper.writeValueAsString(envelope);

        PaymentOutbox outbox = PaymentOutbox.createPending(envelope.header().eventId(), failedTopic, json);

        outboxRepository.save(outbox);

        log.info("[PAYMENT_FAILED_OUTBOX_SAVED] 결제 검증 실패 아웃박스 저장 eventId={}, topic={}, relType={}, relId={}",
                envelope.header().eventId(), failedTopic, event.relType(), event.relId());
    }
}
