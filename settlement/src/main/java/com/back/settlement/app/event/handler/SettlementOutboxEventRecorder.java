package com.back.settlement.app.event.handler;

import com.back.common.event.Envelope;
import com.back.settlement.adapter.out.SettlementOutboxRepository;
import com.back.settlement.app.event.payload.PayoutRequestPayload;
import com.back.settlement.domain.event.SettlementInternalCompletedEvent;
import com.back.settlement.domain.outbox.SettlementOutboxEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Component
public class SettlementOutboxEventRecorder {
    private final SettlementOutboxRepository outboxRepository;
    private final JsonMapper jsonMapper;
    private final String topic;

    public SettlementOutboxEventRecorder(SettlementOutboxRepository outboxRepository, JsonMapper jsonMapper, @Value("${custom.kafka.topic.settlement-payout-requested}") String topic) {
        this.outboxRepository = outboxRepository;
        this.jsonMapper = jsonMapper;
        this.topic = topic;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveToOutbox(SettlementInternalCompletedEvent event) {
        String payload = serialize(event);
        SettlementOutboxEvent outbox = SettlementOutboxEvent.createPending(topic, payload);
        outboxRepository.save(outbox);
    }

    private String serialize(SettlementInternalCompletedEvent event) {
        PayoutRequestPayload payload = PayoutRequestPayload.from(event);
        Envelope<PayoutRequestPayload> envelope = Envelope.of(PayoutRequestPayload.EVENT_TYPE, payload);
        return jsonMapper.writeValueAsString(envelope);
    }
}
