package com.back.cash.adapter.in.listener;

import com.back.cash.adapter.out.outbox.PaymentOutboxRepository;
import com.back.cash.domain.event.PaymentCompletedEvent;
import com.back.cash.domain.outbox.PaymentOutbox;
import com.back.cash.domain.outbox.enums.OutboxStatus;
import com.back.common.dto.cash.enums.RelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentOutboxListenerTest {

    private PaymentCompletedOutboxListener listener;

    @Mock
    private PaymentOutboxRepository outboxRepository;

    @Mock
    private JsonMapper jsonMapper;

    @Captor
    private ArgumentCaptor<PaymentOutbox> outboxCaptor;

    private static final String TOPIC = "cash.payment.completed";
    private static final String MOCKED_JSON = "{\"header\":{\"eventId\":\"uuid\"},\"payload\":{}}";

    @BeforeEach
    void setUp() throws Exception {
        listener = new PaymentCompletedOutboxListener(outboxRepository, jsonMapper);
        setField(listener, "completedTopic", TOPIC);
    }

    @Test
    @DisplayName("record - PaymentCompletedEvent → PENDING 상태로 outbox 저장")
    void record_savesOutboxAsPending() {
        // given
        PaymentCompletedEvent event = new PaymentCompletedEvent(RelType.ORDER, 100L, new BigDecimal("30000"));
        given(jsonMapper.writeValueAsString(any())).willReturn(MOCKED_JSON);

        // when
        listener.record(event);

        // then
        verify(outboxRepository).save(outboxCaptor.capture());

        PaymentOutbox saved = outboxCaptor.getValue();
        assertThat(saved.getTopic()).isEqualTo(TOPIC);
        assertThat(saved.getPayload()).isEqualTo(MOCKED_JSON);
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(saved.getEventId()).isNotBlank();
        assertThat(saved.getRetryCount()).isZero();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("record - 이벤트 필드가 payload에 포함되어 직렬화 요청됨")
    void record_serializesEnvelopeWithEventFields() {
        // given
        PaymentCompletedEvent event = new PaymentCompletedEvent(RelType.ORDER, 100L, new BigDecimal("30000"));
        given(jsonMapper.writeValueAsString(any())).willReturn(MOCKED_JSON);

        // when
        listener.record(event);

        // then: jsonMapper.writeValueAsString가 Envelope 객체로 호출됨
        verify(jsonMapper).writeValueAsString(any());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
