package com.back.settlement.app.event.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.back.common.event.Envelope;
import com.back.common.event.KafkaEventPublisher;
import com.back.settlement.app.event.payload.PayoutRequestPayload;
import com.back.settlement.domain.SettlementStatus;
import com.back.settlement.domain.event.SettlementStartedEvent;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("캐시 지급 요청 테스트")
class SettlementCashPayoutEventHandlerTest {

    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    @InjectMocks
    private SettlementCashPayoutEventHandler handler;

    private static final String TOPIC = "settlement-payout-request";

    @Test
    @DisplayName("SettlementStartedEvent 수신 시 올바른 토픽과 payload로 Kafka 이벤트를 발행한다")
    void publishesCorrectPayload() {
        // given
        ReflectionTestUtils.setField(handler, "topic", TOPIC);

        SettlementStartedEvent event = new SettlementStartedEvent(
                1L, SettlementStatus.PENDING, 42L,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(90000),
                BigDecimal.valueOf(10000)
        );

        // when
        handler.settlementStartedEvent(event);

        // then
        ArgumentCaptor<Envelope<PayoutRequestPayload>> captor = ArgumentCaptor.forClass(Envelope.class);
        verify(kafkaEventPublisher).publish(eq(TOPIC), captor.capture());

        Envelope<PayoutRequestPayload> published = captor.getValue();
        assertThat(published.header().eventType()).isEqualTo("settlement.payout.requested");

        PayoutRequestPayload payload = published.payload();
        assertThat(payload.settlementId()).isEqualTo(1L);
        assertThat(payload.payeeId()).isEqualTo(42L);
        assertThat(payload.totalGrossAmount()).isEqualByComparingTo(BigDecimal.valueOf(100000));
        assertThat(payload.totalNetAmount()).isEqualByComparingTo(BigDecimal.valueOf(90000));
        assertThat(payload.totalFeeAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
    }
}
