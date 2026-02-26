package com.back.cash.adapter.in.listener;

import com.back.cash.adapter.out.outbox.PayoutOutboxRepository;
import com.back.cash.domain.event.PayoutFailedEvent;
import com.back.cash.domain.outbox.PayoutOutbox;
import com.back.cash.domain.outbox.enums.OutboxStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PayoutFailedOutboxListenerTest {

    private PayoutFailedOutboxListener listener;

    @Mock
    private PayoutOutboxRepository outboxRepository;

    @Mock
    private JsonMapper jsonMapper;

    @Captor
    private ArgumentCaptor<PayoutOutbox> outboxCaptor;

    private static final String TOPIC = "cash.payout.failed";
    private static final String MOCKED_JSON = "{\"header\":{\"eventId\":\"uuid\"},\"payload\":{}}";

    @BeforeEach
    void setUp() throws Exception {
        listener = new PayoutFailedOutboxListener(outboxRepository, jsonMapper);
        setField(listener, "cashPayoutFailedTopic", TOPIC);
    }

    @Test
    @DisplayName("record - PayoutFailedEvent → PENDING 상태로 outbox 저장")
    void record_savesOutboxAsPending() {
        // given
        PayoutFailedEvent event = new PayoutFailedEvent(123L, "금액 검증 실패");
        given(jsonMapper.writeValueAsString(any())).willReturn(MOCKED_JSON);

        // when
        listener.record(event);

        // then
        verify(outboxRepository).save(outboxCaptor.capture());

        PayoutOutbox saved = outboxCaptor.getValue();
        assertThat(saved.getTopic()).isEqualTo(TOPIC);
        assertThat(saved.getPayload()).isEqualTo(MOCKED_JSON);
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(saved.getEventId()).isNotBlank();
        assertThat(saved.getRetryCount()).isZero();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("record - Envelope 직렬화 요청됨")
    void record_serializesEnvelope() {
        // given
        PayoutFailedEvent event = new PayoutFailedEvent(123L, "금액 검증 실패");
        given(jsonMapper.writeValueAsString(any())).willReturn(MOCKED_JSON);

        // when
        listener.record(event);

        // then
        verify(jsonMapper).writeValueAsString(any());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
