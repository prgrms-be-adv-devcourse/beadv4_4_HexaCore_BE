package com.back.cash.adapter.out;

import com.back.cash.app.event.PaymentCompletedPayload;
import com.back.cash.app.event.PaymentFailPayload;
import com.back.cash.domain.event.PaymentCompletedEvent;
import com.back.cash.domain.event.PaymentFailedEvent;
import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentKafkaPublisher {

    @Value("${custom.kafka.topic.cash-payment-completed}")
    private String paymentCompletedTopic;

    @Value("${custom.kafka.topic.cash-payment-failed}")
    private String paymentFailedTopic;

    private final KafkaEventPublisher kafkaEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishCompleted(PaymentCompletedEvent event) {
        Envelope<PaymentCompletedPayload> envelope = Envelope.of(
                "PAYMENT_COMPLETED",
                new PaymentCompletedPayload(event.relType(), event.relId(), event.totalAmount())
        );

        kafkaEventPublisher.publish(paymentCompletedTopic, envelope);

        log.info("[PAYMENT_KAFKA_SENT] topic={}, relType={}, relId={}",
                paymentCompletedTopic, event.relType(), event.relId());
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishFailed(PaymentFailedEvent event) {
        Envelope<PaymentFailPayload> envelope = Envelope.of(
                "PAYMENT_FAILED",
                new PaymentFailPayload(event.relType(), event.relId())
        );

        kafkaEventPublisher.publish(paymentFailedTopic, envelope);

        log.info("[PAYMENT_KAFKA_SENT] topic={}, relType={}, relId={}",
                paymentFailedTopic, event.relType(), event.relId());
    }
}
