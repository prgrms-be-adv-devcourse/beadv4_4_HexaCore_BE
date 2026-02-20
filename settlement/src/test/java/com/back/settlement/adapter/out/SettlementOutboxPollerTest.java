package com.back.settlement.adapter.out;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.back.settlement.domain.outbox.SettlementOutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementOutboxPoller 단위 테스트")
class SettlementOutboxPollerTest {

    @Mock
    private SettlementOutboxRepository outboxRepository;

    @Mock
    private SettlementOutboxPublisher outboxPublisher;

    @Mock
    private SettlementOutboxStateService outboxStateService;

    private SettlementOutboxPoller poller;

    @BeforeEach
    void setUp() {
        poller = new SettlementOutboxPoller(
                outboxRepository,
                outboxPublisher,
                outboxStateService,
                100,
                10,
                7,
                5
        );
    }

    @Test
    @DisplayName("PENDING 대상이 없으면 발행을 수행하지 않는다")
    void pollPending_WhenNoRows_DoNotPublish() {
        when(outboxStateService.markPending(100)).thenReturn(List.of());

        poller.pollPending();

        verify(outboxStateService).markPending(100);
        verify(outboxPublisher, never()).publish(List.of());
    }

    @Test
    @DisplayName("PENDING 대상이 있으면 발행한다")
    void pollPending_WhenRowsExist_Publish() {
        List<Long> ids = List.of(1L, 2L);
        when(outboxStateService.markPending(100)).thenReturn(ids);

        poller.pollPending();

        verify(outboxStateService).markPending(100);
        verify(outboxPublisher).publish(ids);
    }

    @Test
    @DisplayName("FAILED 대상이 있으면 재발행한다")
    void handleFailure_WhenRowsExist_Publish() {
        List<Long> ids = List.of(3L);
        when(outboxStateService.markFailed(100, 10)).thenReturn(ids);

        poller.handleFailure();

        verify(outboxStateService).markFailed(100, 10);
        verify(outboxPublisher).publish(ids);
    }

    @Test
    @DisplayName("타임아웃된 PROCESSING 이벤트를 복구한다")
    void recoverStuckProcessing_RecoversRows() {
        when(outboxStateService.markTimedOutProcessingAsPending(anyInt())).thenReturn(2);

        poller.recoverStuckProcessing();

        verify(outboxStateService).markTimedOutProcessingAsPending(5);
    }

    @Test
    @DisplayName("보관 기간이 지난 SENT 이벤트를 삭제한다")
    void deleteOldSentEvents_DeletesExpiredRows() {
        when(outboxRepository.deleteOldSent(eq(SettlementOutboxStatus.SENT), any(LocalDateTime.class))).thenReturn(3);

        poller.deleteOldSentEvents();

        verify(outboxRepository).deleteOldSent(eq(SettlementOutboxStatus.SENT), any(LocalDateTime.class));
    }
}
