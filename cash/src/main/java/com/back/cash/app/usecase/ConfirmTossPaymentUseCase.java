package com.back.cash.app.usecase;

import com.back.cash.adapter.out.PaymentRepository;
import com.back.cash.adapter.out.TossPaymentsClient;
import com.back.cash.app.CashLogSupport;
import com.back.cash.app.WalletSupport;
import com.back.cash.domain.Payment;
import com.back.cash.domain.Wallet;
import com.back.cash.domain.enums.PaymentStatus;
import com.back.cash.dto.request.PaymentCompletedRequestDto;
import com.back.cash.dto.request.PaymentFailedRequestDto;
import com.back.cash.dto.request.TossConfirmRequest;
import com.back.cash.dto.response.ConfirmResultResponseDto;
import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ConfirmTossPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final WalletSupport walletSupport;
    private final CashLogSupport cashLogSupport;
    private final TossPaymentsClient tossPaymentsClient;

    public ConfirmResultResponseDto execute(TossConfirmRequest req) {

        Payment payment = paymentRepository.findWithLockByTossOrderId(req.orderId())
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.PAYMENT_NOT_FOUND));

        // 이미 DONE이면 종료
        if (payment.getStatus() == PaymentStatus.DONE) {
            return ConfirmResultResponseDto.success(toCompletedDto(payment));
        }

        // PG 대상/금액 검증
        if (payment.getPgAmount() == null || payment.getPgAmount().signum() <= 0) {
            throw new BadRequestException(FailureCode.INVALID_CONFIRM);
        }
        if (payment.getPgAmount().compareTo(req.amount()) != 0) {
            throw new BadRequestException(FailureCode.AMOUNT_MISMATCH);
        }

        // 토스 confirm 호출
        try {
            tossPaymentsClient.confirm(req.paymentKey(), req.orderId(), req.amount());
        } catch (Exception e) {
            // confirm 실패: FAIL + 선홀딩(walletUsedAmount) 되돌림(RELEASE)
            handleConfirmFail(payment);
            return ConfirmResultResponseDto.fail(toFailedDto(payment));
        }

        // confirm 성공: PG 금액을 유저에 충전됐다가 즉시 홀딩으로 이동
        applyPgTopUpThenHold(payment);

        payment.setPaymentKeyIfAbsent(req.paymentKey());
        payment.markAsDone();

        return ConfirmResultResponseDto.success(toCompletedDto(payment));
    }

    private PaymentCompletedRequestDto toCompletedDto(Payment payment) {
        return new PaymentCompletedRequestDto(
                payment.getRelType(),
                payment.getRelId(),
                payment.getTotalAmount()
        );
    }

    private PaymentFailedRequestDto toFailedDto(Payment payment) {
        return new PaymentFailedRequestDto(
                payment.getRelType(),
                payment.getRelId()
        );
    }

    private void applyPgTopUpThenHold(Payment payment) {
        Wallet buyerWallet = walletSupport.getUserWallet(payment.getUserId());
        Wallet systemWallet = walletSupport.getSystemWallet();

        BigDecimal pgAmount = payment.getPgAmount();

        // 유저 지갑에 PG 결제 금액만큼 충전
        buyerWallet.deposit(pgAmount);
        cashLogSupport.recordUserPgTopUpLog(buyerWallet, pgAmount, payment.getRelType(), payment.getRelId());

        // PG 결제 금액 즉시 홀딩(유저 -> 시스템)
        buyerWallet.withdraw(pgAmount);
        systemWallet.deposit(pgAmount);
        cashLogSupport.recordHoldingLog(buyerWallet, systemWallet, pgAmount, payment.getRelType(), payment.getRelId());
    }

    private void handleConfirmFail(Payment payment) {
        payment.markAsFail();

        BigDecimal held = payment.getWalletUsedAmount(); // payAndHold에서 이미 홀딩된 예치금
        if (held == null || held.signum() <= 0) return;

        if (payment.isReleased()) return;

        Wallet buyerWallet = walletSupport.getUserWallet(payment.getUserId());
        Wallet systemWallet = walletSupport.getSystemWallet();

        // 홀딩 되돌리기: 시스템 -held / 유저 +held
        systemWallet.withdraw(held);
        buyerWallet.deposit(held);

        cashLogSupport.recordReleaseOnPaymentFail(buyerWallet, systemWallet, held, payment.getRelType(), payment.getRelId());

        payment.markReleased();
    }
}
