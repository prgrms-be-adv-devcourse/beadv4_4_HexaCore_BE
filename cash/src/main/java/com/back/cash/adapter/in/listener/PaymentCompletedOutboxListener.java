package com.back.cash.adapter.in.listener;

import com.back.cash.adapter.out.outbox.PaymentCompletedOutboxRepository;
import com.back.cash.app.event.PaymentCompletedPayload;
import com.back.cash.domain.event.PaymentCompletedEvent;
import com.back.cash.domain.outbox.PaymentCompletedOutbox;
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
public class PaymentCompletedOutboxListener {

    private final PaymentCompletedOutboxRepository outboxRepository;
    private final JsonMapper jsonMapper;

    @Value("${custom.kafka.topic.cash-payment-completed}")
    private String completedTopic;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(PaymentCompletedEvent event) {
        PaymentCompletedPayload payload = new PaymentCompletedPayload(event.relType(), event.relId(), event.totalAmount());

        Envelope<PaymentCompletedPayload> envelope = Envelope.of(completedTopic, payload);
        String json = jsonMapper.writeValueAsString(envelope);

        PaymentCompletedOutbox outbox = PaymentCompletedOutbox.createPending(envelope.header().eventId(), completedTopic, json);

        outboxRepository.save(outbox);

        log.info("[PAYMENT_COMPLETED_OUTBOX_SAVED] eventId={}, topic={}, relType={}, relId={}",
                envelope.header().eventId(), completedTopic, event.relType(), event.relId());
    }

}
