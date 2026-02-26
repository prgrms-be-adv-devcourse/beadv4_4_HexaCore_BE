package com.back.cash.adapter.in.listener;

import com.back.cash.adapter.out.outbox.PayoutOutboxRepository;
import com.back.cash.app.event.PayoutFailedPayload;
import com.back.cash.domain.event.PayoutFailedEvent;
import com.back.cash.domain.outbox.PayoutOutbox;
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
public class PayoutFailedOutboxListener {

    private final PayoutOutboxRepository outboxRepository;
    private final JsonMapper jsonMapper;

    @Value("${custom.kafka.topic.cash-payout-failed}")
    private String cashPayoutFailedTopic;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(PayoutFailedEvent event) {
        PayoutFailedPayload payload = new PayoutFailedPayload(event.settlementId(), event.reason());

        Envelope<PayoutFailedPayload> envelope = Envelope.of(cashPayoutFailedTopic, payload);
        String json = jsonMapper.writeValueAsString(envelope);

        PayoutOutbox outbox = PayoutOutbox.createPending(envelope.header().eventId(), cashPayoutFailedTopic, json);
        outboxRepository.save(outbox);

        log.info("[PAYOUT_FAILED_OUTBOX_SAVED] 정산 실패 아웃박스 저장 eventId={}, settlementId={}",
                envelope.header().eventId(), event.settlementId());
    }
}
