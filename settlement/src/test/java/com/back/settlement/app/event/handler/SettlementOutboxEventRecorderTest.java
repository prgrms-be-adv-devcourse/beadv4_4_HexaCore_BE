package com.back.settlement.app.event.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.back.settlement.adapter.out.SettlementOutboxRepository;
import com.back.settlement.domain.SettlementStatus;
import com.back.settlement.domain.event.SettlementInternalCompletedEvent;
import com.back.settlement.domain.outbox.SettlementOutboxEvent;
import com.back.settlement.domain.outbox.SettlementOutboxStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementOutboxEventRecorder 단위 테스트")
class SettlementOutboxEventRecorderTest {

    private static final String TOPIC = "settlement.payout.requested";

    @Mock
    private SettlementOutboxRepository outboxRepository;

    @Mock
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("내부 완료 이벤트를 outbox PENDING row로 저장한다")
    void saveToOutbox_SavesPendingEvent() {
        // given
        SettlementOutboxEventRecorder recorder = new SettlementOutboxEventRecorder(outboxRepository, jsonMapper, TOPIC);
        SettlementInternalCompletedEvent event = completedEvent();
        given(jsonMapper.writeValueAsString(any())).willReturn("{\"ok\":true}");

        // when
        recorder.saveToOutbox(event);

        // then
        ArgumentCaptor<SettlementOutboxEvent> captor = ArgumentCaptor.forClass(SettlementOutboxEvent.class);
        verify(outboxRepository).save(captor.capture());

        SettlementOutboxEvent saved = captor.getValue();
        assertThat(saved.getTopic()).isEqualTo(TOPIC);
        assertThat(saved.getStatus()).isEqualTo(SettlementOutboxStatus.PENDING);
        assertThat(saved.getRetryCount()).isZero();
        assertThat(saved.getPayload()).isEqualTo("{\"ok\":true}");
        assertThat(saved.getCreateDate()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("직렬화 실패 시 예외를 전파하고 outbox를 저장하지 않는다")
    void saveToOutbox_WhenSerializationFails_ThrowsAndDoesNotSave() {
        // given
        SettlementOutboxEventRecorder recorder = new SettlementOutboxEventRecorder(outboxRepository, jsonMapper, TOPIC);
        SettlementInternalCompletedEvent event = completedEvent();
        given(jsonMapper.writeValueAsString(any())).willThrow(new IllegalStateException("serialize failed"));

        // when & then
        assertThatThrownBy(() -> recorder.saveToOutbox(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("serialize failed");

        verify(outboxRepository, never()).save(any());
    }

    private SettlementInternalCompletedEvent completedEvent() {
        return new SettlementInternalCompletedEvent(
                1L,
                SettlementStatus.PENDING,
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(9000),
                77L,
                "seller",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
