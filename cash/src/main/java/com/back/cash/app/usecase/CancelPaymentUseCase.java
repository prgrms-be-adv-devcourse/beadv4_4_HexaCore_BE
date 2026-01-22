package com.back.cash.app.usecase;

import com.back.cash.adapter.out.PaymentRepository;
import com.back.cash.app.CashLogSupport;
import com.back.cash.app.WalletSupport;
import com.back.cash.domain.Payment;
import com.back.cash.domain.Wallet;
import com.back.cash.domain.enums.PaymentStatus;
import com.back.common.code.FailureCode;
import com.back.common.dto.cash.request.PaymentCancelRequestDto;
import com.back.common.dto.cash.response.PaymentCancelResponseDto;
import com.back.common.exception.CustomException;
import com.back.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CancelPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final WalletSupport walletSupport;
    private final CashLogSupport cashLogSupport;

    public PaymentCancelResponseDto execute(PaymentCancelRequestDto req) {

        Payment payment = paymentRepository.findWithLockByRelTypeAndRelId(req.relType(), req.relId())
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.PAYMENT_NOT_FOUND));

        // 요청자 검증
        if (!payment.getUserId().equals(req.userId())) {
            throw new CustomException(FailureCode.OWNER_MISMATCH);
        }

        BigDecimal expected = refundableAmount(payment);

        // 이미 취소면 그대로 응답
        if (payment.getStatus() == PaymentStatus.CANCELED) {
            if (!payment.isReleased()) {
                throw new CustomException(FailureCode.PAYMENT_STATE_INCONSISTENT);
            }
            return PaymentCancelResponseDto.of(payment.getUserId(), expected);
        }

        // DONE이 아니면 취소 불가
        if (payment.getStatus() != PaymentStatus.DONE) {
            throw new CustomException(FailureCode.INVALID_CANCEL);
        }

        // DONE인데 releasedAt이 있으면 비정상
        if (payment.isReleased()) {
            throw new CustomException(FailureCode.PAYMENT_STATE_INCONSISTENT);
        }

        // 금액 검증
        if (expected.compareTo(req.amount()) != 0) {
            throw new CustomException(FailureCode.AMOUNT_MISMATCH);
        }

        // 환불(홀딩 해제): 시스템 -> 유저
        Wallet buyerWallet = walletSupport.getUserWallet(payment.getUserId());
        Wallet systemWallet = walletSupport.getSystemWallet();

        systemWallet.withdraw(expected);
        buyerWallet.deposit(expected);

        // 로그
        cashLogSupport.recordCancelRefund(
                buyerWallet,
                systemWallet,
                expected,
                payment.getRelType(),
                payment.getRelId()
        );

        payment.markReleased();
        payment.markAsCanceled();

        return PaymentCancelResponseDto.of(payment.getUserId(), expected);
    }

    private BigDecimal refundableAmount(Payment payment) {
        if (payment.getTotalAmount() == null) {
            throw new CustomException(FailureCode.PAYMENT_TOTAL_AMOUNT_MISSING);
        }
        return payment.getTotalAmount();
    }
}

