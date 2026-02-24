package com.back.cash.adapter.out;

import com.back.cash.app.event.PaymentFailPayload;
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

    private static final String FAILED_TOPIC = "cash.payment.failed";

    @BeforeEach
    void setUp() throws Exception {
        publisher = new PaymentKafkaPublisher(kafkaEventPublisher);
        setField(publisher, "paymentFailedTopic", FAILED_TOPIC);
    }

    @Test
    @DisplayName("[publishFailed] PaymentFailedEvent → PAYMENT_FAILED Envelope로 발행")
    void publishFailed_sendsEnvelopeWithCorrectPayload() {
        // given
        PaymentFailedEvent event = new PaymentFailedEvent(RelType.BIDDING, 200L, "FAIL_REASON");

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
        assertThat(payload.failReason()).isEqualTo("FAIL_REASON");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
