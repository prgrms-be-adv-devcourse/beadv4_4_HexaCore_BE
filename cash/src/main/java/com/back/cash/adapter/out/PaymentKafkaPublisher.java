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
                "cash.payment.completed",
                new PaymentCompletedPayload(event.relType(), event.relId(), event.totalAmount())
        );

        kafkaEventPublisher.publish(paymentCompletedTopic, envelope);

        log.info("[PAYMENT_COMPLETED_KAFKA_PUBLISH] 결제 검증 완료 이벤트 발행 eventId={}, occurredAt={}, topic={}, relType={}, relId={}",
                envelope.header().eventId(), envelope.header().occurrenceAt(), paymentCompletedTopic, event.relType(), event.relId());
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishFailed(PaymentFailedEvent event) {
        Envelope<PaymentFailPayload> envelope = Envelope.of(
                "cash.payment.failed",
                new PaymentFailPayload(event.relType(), event.relId())
        );

        kafkaEventPublisher.publish(paymentFailedTopic, envelope);

        log.info("[PAYMENT_FAILED_KAFKA_PUBLISH] 결제 검증 실패 이벤트 발행 eventId={}, occurredAt={}, topic={}, relType={}, relId={}",
                envelope.header().eventId(), envelope.header().occurrenceAt(), paymentFailedTopic, event.relType(), event.relId());
    }
}
