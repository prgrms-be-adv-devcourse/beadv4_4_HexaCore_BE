package com.back.cash;

import com.back.cash.adapter.out.market.MarketPaymentsClient;
import com.back.cash.app.CashFacade;
import com.back.cash.app.usecase.CancelPaymentUseCase;
import com.back.cash.app.usecase.ConfirmTossPaymentUseCase;
import com.back.cash.app.usecase.FailTossPaymentUseCase;
import com.back.cash.app.usecase.PayAndHoldUseCase;
import com.back.cash.dto.request.TossConfirmRequest;
import com.back.cash.dto.response.ConfirmResultResponseDto;
import com.back.common.dto.cash.enums.RelType;
import com.back.common.dto.cash.request.PaymentCompletedRequestDto;
import com.back.common.dto.cash.request.PaymentFailedRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashFacadeTest {

    @InjectMocks
    private CashFacade cashFacade;

    @Mock
    private PayAndHoldUseCase payAndHoldUseCase;
    @Mock
    private ConfirmTossPaymentUseCase confirmTossPaymentUseCase;
    @Mock
    private MarketPaymentsClient marketPaymentsClient;
    @Mock
    private FailTossPaymentUseCase failTossPaymentUseCase;
    @Mock
    private CancelPaymentUseCase cancelPaymentUseCase;

    private static final String ORDER_ID = "order_test_123";
    private static final String PAYMENT_KEY = "pk_test_abc";
    private static final RelType REL_TYPE = RelType.ORDER;
    private static final Long REL_ID = 100L;

    @Test
    @DisplayName("[confirmTossPayment] 결제 성공 시 마켓에 성공 알림 전송")
    void confirmTossPayment_whenSuccess_thenNotifyCompleted() {
        // given
        TossConfirmRequest req = new TossConfirmRequest(PAYMENT_KEY, ORDER_ID, bd("18000"));
        PaymentCompletedRequestDto completedDto = new PaymentCompletedRequestDto(REL_TYPE, REL_ID, bd("30000"));
        ConfirmResultResponseDto successResult = ConfirmResultResponseDto.success(completedDto);

        given(confirmTossPaymentUseCase.execute(req)).willReturn(successResult);

        // when
        ConfirmResultResponseDto result = cashFacade.confirmTossPayment(req);

        // then
        assertThat(result.isSuccess()).isTrue();
        verify(marketPaymentsClient).notifyPaymentCompleted(completedDto);
        verify(marketPaymentsClient, never()).notifyPaymentFailed(any());
    }

    @Test
    @DisplayName("[confirmTossPayment] 결제 실패 시 마켓에 실패 알림 전송")
    void confirmTossPayment_whenFail_thenNotifyFailed() {
        // given
        TossConfirmRequest req = new TossConfirmRequest(PAYMENT_KEY, ORDER_ID, bd("18000"));
        PaymentFailedRequestDto failedDto = new PaymentFailedRequestDto(REL_TYPE, REL_ID);
        ConfirmResultResponseDto failResult = ConfirmResultResponseDto.fail(failedDto);

        given(confirmTossPaymentUseCase.execute(req)).willReturn(failResult);

        // when
        ConfirmResultResponseDto result = cashFacade.confirmTossPayment(req);

        // then
        assertThat(result.isSuccess()).isFalse();
        verify(marketPaymentsClient).notifyPaymentFailed(failedDto);
        verify(marketPaymentsClient, never()).notifyPaymentCompleted(any());
    }

    @Test
    @DisplayName("[confirmTossPayment] 마켓 알림 예외 발생해도 결과는 정상 반환 (예외 삼킴)")
    void confirmTossPayment_whenMarketNotifyFails_thenStillReturnResult() {
        // given
        TossConfirmRequest req = new TossConfirmRequest(PAYMENT_KEY, ORDER_ID, bd("18000"));
        PaymentCompletedRequestDto completedDto = new PaymentCompletedRequestDto(REL_TYPE, REL_ID, bd("30000"));
        ConfirmResultResponseDto successResult = ConfirmResultResponseDto.success(completedDto);

        given(confirmTossPaymentUseCase.execute(req)).willReturn(successResult);
        doThrow(new RuntimeException("Market API error"))
                .when(marketPaymentsClient).notifyPaymentCompleted(completedDto);

        // when
        ConfirmResultResponseDto result = cashFacade.confirmTossPayment(req);

        // then: 예외가 전파되지 않고 정상 반환
        assertThat(result.isSuccess()).isTrue();
        assertThat(result).isEqualTo(successResult);
        verify(marketPaymentsClient).notifyPaymentCompleted(completedDto);
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }
}
