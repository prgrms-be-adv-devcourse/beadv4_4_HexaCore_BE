package com.back.cash.app;

import com.back.cash.adapter.out.PayoutRepository;
import com.back.cash.app.usecase.CashPayoutUseCase;
import com.back.cash.domain.event.CashPayoutRequestedCommand;
import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashPayoutFacadeTest {

    @Mock
    private CashPayoutUseCase cashPayoutUseCase;

    @Mock
    private PayoutRepository payoutRepository;

    @InjectMocks
    private CashPayoutFacade cashPayoutFacade;

    @Test
    @DisplayName("DataIntegrityViolationException + 중복 정산이면 예외를 삼키고 종료한다")
    void duplicateSettlement_swallowDataIntegrityViolation() {
        CashPayoutRequestedCommand event = event(100L);
        doThrow(new DataIntegrityViolationException("duplicate key"))
                .when(cashPayoutUseCase).execute(event);
        when(payoutRepository.existsBySettlementId(100L)).thenReturn(true);

        cashPayoutFacade.requestPayout(event);

        verify(cashPayoutUseCase, never()).saveFailedPayout(any(), any());
    }

    @Test
    @DisplayName("DataIntegrityViolationException + 중복 아니면 예외를 전파한다")
    void nonDuplicateDataIntegrityViolation_rethrow() {
        CashPayoutRequestedCommand event = event(101L);
        doThrow(new DataIntegrityViolationException("db constraint"))
                .when(cashPayoutUseCase).execute(event);
        when(payoutRepository.existsBySettlementId(101L)).thenReturn(false);

        assertThatThrownBy(() -> cashPayoutFacade.requestPayout(event))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(cashPayoutUseCase, never()).saveFailedPayout(any(), any());
    }

    @Test
    @DisplayName("BadRequestException이면 실패 마킹 후 종료한다")
    void badRequest_saveFailedAndReturn() {
        CashPayoutRequestedCommand event = event(102L);
        doThrow(new BadRequestException(FailureCode.INVALID_AMOUNT))
                .when(cashPayoutUseCase).execute(event);

        cashPayoutFacade.requestPayout(event);

        verify(cashPayoutUseCase, times(1)).saveFailedPayout(eq(event), anyString());
    }

    @Test
    @DisplayName("일반 Exception이면 예외를 전파한다 (재시도 대상)")
    void generalException_rethrow() {
        CashPayoutRequestedCommand event = event(103L);
        doThrow(new RuntimeException("DB 타임아웃"))
                .when(cashPayoutUseCase).execute(event);

        assertThatThrownBy(() -> cashPayoutFacade.requestPayout(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 타임아웃");

        verify(cashPayoutUseCase, never()).saveFailedPayout(any(), any());
    }

    private CashPayoutRequestedCommand event(Long settlementId) {
        return new CashPayoutRequestedCommand(
                settlementId,
                1L,
                new BigDecimal("10000"),
                new BigDecimal("9000"),
                new BigDecimal("1000")
        );
    }
}
