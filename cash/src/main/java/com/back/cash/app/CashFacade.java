package com.back.cash.app;

import com.back.cash.app.usecase.CancelPaymentUseCase;
import com.back.cash.app.usecase.ConfirmTossPaymentUseCase;
import com.back.cash.app.usecase.FailTossPaymentUseCase;
import com.back.cash.app.usecase.PayAndHoldUseCase;
import com.back.cash.dto.request.TossConfirmRequest;
import com.back.cash.dto.request.TossFailRequestDto;
import com.back.cash.dto.response.ConfirmResultResponseDto;
import com.back.common.dto.cash.request.PayAndHoldRequestDto;
import com.back.common.dto.cash.request.PaymentCancelRequestDto;
import com.back.common.dto.cash.response.PayAndHoldResponseDto;
import com.back.common.dto.cash.response.PaymentCancelResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashFacade {

    private final PayAndHoldUseCase payAndHoldUseCase;
    private final ConfirmTossPaymentUseCase confirmTossPaymentUseCase;
    private final FailTossPaymentUseCase failTossPaymentUseCase;
    private final CancelPaymentUseCase cancelPaymentUseCase;

    @Transactional
    public PayAndHoldResponseDto payAndHold(PayAndHoldRequestDto dto) {
        return payAndHoldUseCase.execute(dto);
    }

    /**
     * 토스 결제 확인 처리
     * 마켓 통보는 트랜잭션 커밋 후 Kafka로 발행
     */
    public ConfirmResultResponseDto confirmTossPayment(TossConfirmRequest req) {
        return confirmTossPaymentUseCase.execute(req);
    }

    @Transactional
    public void failTossPayment(TossFailRequestDto req) {
        failTossPaymentUseCase.execute(req);
    }

    @Transactional
    public PaymentCancelResponseDto cancelPayment(PaymentCancelRequestDto req) {
        return cancelPaymentUseCase.execute(req);
    }
}
