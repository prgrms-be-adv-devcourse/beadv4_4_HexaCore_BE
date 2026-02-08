package com.back.settlement.app.event.handler;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.settlement.app.event.payload.PayoutRequestPayload;
import com.back.settlement.domain.event.SettlementStartedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class SettlementCashPayoutEventHandler {
    private final KafkaEventPublisher kafkaEventPublisher;
    private final String topic;

    public SettlementCashPayoutEventHandler(KafkaEventPublisher kafkaEventPublisher, @Value("${kafka.topic.settlement-payout-request}") String topic) {
        this.kafkaEventPublisher = kafkaEventPublisher;
        this.topic = topic;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishPayoutRequest(SettlementStartedEvent event) {
        Envelope<PayoutRequestPayload> kafkaEvent = Envelope.of(
                "settlement.payout.requested",
                new PayoutRequestPayload(
                        event.settlementId(),
                        event.sellerId(),
                        event.totalGrossAmount(),
                        event.totalNetAmount(),
                        event.totalFeeAmount()
                )
        );
        kafkaEventPublisher.publish(topic, kafkaEvent);
    }
}
