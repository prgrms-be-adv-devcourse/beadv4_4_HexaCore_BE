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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Slf4j
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

    /**
     * 토스 결제 확인 처리
     */
    public ConfirmResultResponseDto confirmTossPayment(TossConfirmRequest req) {
        ConfirmResultResponseDto result = confirmTossPaymentUseCase.execute(req);

        if (!result.isPending()) {
            notifyMarket(result);
        }
        // todo: pending인 경우 마켓에게 응답 안하는데 처리 필요
        return result;
    }

    private void notifyMarket(ConfirmResultResponseDto result) {
        try {
            if (result.isSuccess()) {
                marketPaymentsClient.notifyPaymentCompleted(result.completedDto());
            } else {
                marketPaymentsClient.notifyPaymentFailed(result.failedDto());
            }
        } catch (Exception e) {
            // TODO: 실패 시 처리
            log.error("[MARKET_NOTIFY_FAIL] success={}, dto={}", result.isSuccess(),
                    result.isSuccess() ? result.completedDto() : result.failedDto(), e);
        }
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
