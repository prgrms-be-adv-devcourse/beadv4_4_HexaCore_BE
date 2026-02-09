package com.back.cash;

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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CashFacadeTest {

    @InjectMocks
    private CashFacade cashFacade;

    @Mock
    private PayAndHoldUseCase payAndHoldUseCase;
    @Mock
    private ConfirmTossPaymentUseCase confirmTossPaymentUseCase;
    @Mock
    private FailTossPaymentUseCase failTossPaymentUseCase;
    @Mock
    private CancelPaymentUseCase cancelPaymentUseCase;

    private static final String ORDER_ID = "order_test_123";
    private static final String PAYMENT_KEY = "pk_test_abc";
    private static final RelType REL_TYPE = RelType.ORDER;
    private static final Long REL_ID = 100L;

    @Test
    @DisplayName("[confirmTossPayment] 결제 성공 시 성공 결과 반환")
    void confirmTossPayment_whenSuccess_thenReturnResult() {
        // given
        TossConfirmRequest req = new TossConfirmRequest(PAYMENT_KEY, ORDER_ID, bd("18000"));
        PaymentCompletedRequestDto completedDto = new PaymentCompletedRequestDto(REL_TYPE, REL_ID, bd("30000"));
        ConfirmResultResponseDto successResult = ConfirmResultResponseDto.success(completedDto);

        given(confirmTossPaymentUseCase.execute(req)).willReturn(successResult);

        // when
        ConfirmResultResponseDto result = cashFacade.confirmTossPayment(req);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.completedDto()).isEqualTo(completedDto);
        assertThat(result.failReason()).isNull();
    }

    @Test
    @DisplayName("[confirmTossPayment] 결제 실패 시 실패 결과와 사유 반환")
    void confirmTossPayment_whenFail_thenReturnFailWithReason() {
        // given
        TossConfirmRequest req = new TossConfirmRequest(PAYMENT_KEY, ORDER_ID, bd("18000"));
        PaymentFailedRequestDto failedDto = new PaymentFailedRequestDto(REL_TYPE, REL_ID);
        ConfirmResultResponseDto failResult = ConfirmResultResponseDto.fail(failedDto, "TOSS_CONFIRM_REJECTED");

        given(confirmTossPaymentUseCase.execute(req)).willReturn(failResult);

        // when
        ConfirmResultResponseDto result = cashFacade.confirmTossPayment(req);

        // then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.failReason()).isEqualTo("TOSS_CONFIRM_REJECTED");
    }

    @Test
    @DisplayName("[confirmTossPayment] PENDING 응답 시 PENDING 결과 반환")
    void confirmTossPayment_whenPending_thenReturnPending() {
        // given
        TossConfirmRequest req = new TossConfirmRequest(PAYMENT_KEY, ORDER_ID, bd("18000"));
        ConfirmResultResponseDto pendingResult = ConfirmResultResponseDto.pending();

        given(confirmTossPaymentUseCase.execute(req)).willReturn(pendingResult);

        // when
        ConfirmResultResponseDto result = cashFacade.confirmTossPayment(req);

        // then
        assertThat(result.isPending()).isTrue();
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }
}
