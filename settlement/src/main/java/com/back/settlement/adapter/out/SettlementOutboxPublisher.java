package com.back.settlement.adapter.out;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SettlementOutboxPublisher {
    private final SettlementOutboxEventSender outboxEventSender;

    public SettlementOutboxPublisher(SettlementOutboxEventSender outboxEventSender) {
        this.outboxEventSender = outboxEventSender;
    }

    public void publish(List<Long> outboxIds) {
        for (Long outboxId : outboxIds) {
            outboxEventSender.send(outboxId);
        }
    }
}
