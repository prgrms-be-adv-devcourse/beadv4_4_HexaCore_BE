package com.back.cash.adapter.out;

import com.back.cash.app.event.PaymentCompletedPayload;
import com.back.cash.app.event.PaymentFailPayload;
import com.back.cash.domain.event.PaymentCompletedEvent;
import com.back.cash.domain.event.PaymentFailedEvent;
import com.back.common.dto.cash.enums.RelType;
import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentKafkaPublisherTest {

    private PaymentKafkaPublisher publisher;

    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    @Captor
    private ArgumentCaptor<Envelope<?>> envelopeCaptor;

    private static final String COMPLETED_TOPIC = "cash.payment.completed";
    private static final String FAILED_TOPIC = "cash.payment.failed";

    @BeforeEach
    void setUp() throws Exception {
        publisher = new PaymentKafkaPublisher(kafkaEventPublisher);
        setField(publisher, "paymentCompletedTopic", COMPLETED_TOPIC);
        setField(publisher, "paymentFailedTopic", FAILED_TOPIC);
    }

    @Test
    @DisplayName("[publishCompleted] PaymentCompletedEvent → PAYMENT_COMPLETED Envelope로 발행")
    void publishCompleted_sendsEnvelopeWithCorrectPayload() {
        // given
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                RelType.ORDER, 100L, new BigDecimal("30000")
        );

        // when
        publisher.publishCompleted(event);

        // then
        verify(kafkaEventPublisher).publish(eq(COMPLETED_TOPIC), envelopeCaptor.capture());

        Envelope<?> envelope = envelopeCaptor.getValue();
        assertThat(envelope.header().eventType()).isEqualTo(COMPLETED_TOPIC);
        assertThat(envelope.header().eventId()).isNotBlank();
        assertThat(envelope.header().occurrenceAt()).isNotNull();

        PaymentCompletedPayload payload = (PaymentCompletedPayload) envelope.payload();
        assertThat(payload.relType()).isEqualTo(RelType.ORDER);
        assertThat(payload.relId()).isEqualTo(100L);
        assertThat(payload.totalAmount()).isEqualByComparingTo("30000");
    }

    @Test
    @DisplayName("[publishFailed] PaymentFailedEvent → PAYMENT_FAILED Envelope로 발행")
    void publishFailed_sendsEnvelopeWithCorrectPayload() {
        // given
        PaymentFailedEvent event = new PaymentFailedEvent(RelType.BIDDING, 200L);

        // when
        publisher.publishFailed(event);

        // then
        verify(kafkaEventPublisher).publish(eq(FAILED_TOPIC), envelopeCaptor.capture());

        Envelope<?> envelope = envelopeCaptor.getValue();
        assertThat(envelope.header().eventType()).isEqualTo(FAILED_TOPIC);
        assertThat(envelope.header().eventId()).isNotBlank();

        PaymentFailPayload payload = (PaymentFailPayload) envelope.payload();
        assertThat(payload.relType()).isEqualTo(RelType.BIDDING);
        assertThat(payload.relId()).isEqualTo(200L);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
