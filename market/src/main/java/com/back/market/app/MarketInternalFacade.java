package com.back.market.app;

import com.back.market.app.usecase.ConfirmPaymentUseCase;
import com.back.common.feign.cash.request.PaymentCompletedRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarketInternalFacade {
    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    /**
     * Cash 모듈로부터 결제 완료(입금 확인) 통지를 수신하여 주문 상태를 확정
     * @param requestDto PaymentCompletedRequestDto
     */
    @Transactional
    public boolean confirmPayment(PaymentCompletedRequestDto requestDto) {
        return confirmPaymentUseCase.confirmPayment(requestDto);
    }
}
