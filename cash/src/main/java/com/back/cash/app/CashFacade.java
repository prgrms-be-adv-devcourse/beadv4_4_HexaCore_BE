package com.back.cash.app;

import com.back.cash.adapter.out.market.MarketPaymentsClient;
import com.back.cash.app.usecase.CancelPaymentUseCase;
import com.back.cash.app.usecase.ConfirmTossPaymentUseCase;
import com.back.cash.app.usecase.FailTossPaymentUseCase;
import com.back.cash.app.usecase.PayAndHoldUseCase;
import com.back.cash.dto.request.TossConfirmRequest;
import com.back.cash.dto.request.TossFailRequestDto;
import com.back.cash.dto.response.ConfirmResultResponseDto;
import com.back.common.dto.cash.request.PayAndHoldRequestDto;
import com.back.common.dto.cash.request.PaymentCancelRequestDto;
import com.back.common.dto.cash.request.PaymentFailedRequestDto;
import com.back.common.dto.cash.response.PayAndHoldResponseDto;
import com.back.common.dto.cash.response.PaymentCancelResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class CashFacade {

    private final PayAndHoldUseCase payAndHoldUseCase;
    private final ConfirmTossPaymentUseCase confirmTossPaymentUseCase;
    private final MarketPaymentsClient marketPaymentsClient;
    private final FailTossPaymentUseCase failTossPaymentUseCase;
    private final CancelPaymentUseCase cancelPaymentUseCase;

    @Transactional
    public PayAndHoldResponseDto payAndHold(PayAndHoldRequestDto dto) {
        return payAndHoldUseCase.execute(dto);
    }

    @Transactional
    public ConfirmResultResponseDto confirmTossPayment(TossConfirmRequest req) {
        ConfirmResultResponseDto result = confirmTossPaymentUseCase.execute(req);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        if (result.isSuccess()) {
                            marketPaymentsClient.notifyPaymentCompleted(result.completedDto());
                        } else {
                            marketPaymentsClient.notifyPaymentFailed(result.failedDto());
                        }
                    }
                }
        );
        return result;
    }

    @Transactional
    public void failTossPayment(TossFailRequestDto req) {
        PaymentFailedRequestDto failedDto = failTossPaymentUseCase.execute(req);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                marketPaymentsClient.notifyPaymentFailed(failedDto);
            }
        });
    }

    @Transactional
    public PaymentCancelResponseDto cancelPayment(PaymentCancelRequestDto req) {
        return cancelPaymentUseCase.execute(req);
    }
}
