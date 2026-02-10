package com.back.cash.app;

import com.back.cash.adapter.out.PaymentRepository;
import com.back.cash.domain.Payment;
import com.back.cash.domain.Wallet;
import com.back.cash.domain.enums.PaymentStatus;
import com.back.cash.dto.request.TossConfirmRequest;
import com.back.cash.dto.response.ConfirmResultResponseDto;
import com.back.cash.mapper.PaymentMapper;
import com.back.common.code.FailureCode;
import com.back.cash.domain.event.PaymentCompletedEvent;
import com.back.cash.domain.event.PaymentFailedEvent;
import com.back.common.exception.BadRequestException;
import com.back.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfirmPaymentSupport {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final WalletSupport walletSupport;
    private final CashLogSupport cashLogSupport;

    /**
     * Payment 조회 및 검증 (트랜잭션)
     */
    @Transactional
    public ConfirmResultResponseDto validatePayment(TossConfirmRequest req) {
        Payment payment = paymentRepository.findWithLockByTossOrderId(req.orderId())
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.PAYMENT_NOT_FOUND));

        // 이미 DONE이면 바로 성공 응답 반환
        if (payment.getStatus() == PaymentStatus.DONE) {
            return ConfirmResultResponseDto.success(PaymentMapper.toCompletedDto(payment));
        }

        // FAIL이나 CANCELED면 처리 불가
        if (payment.getStatus() == PaymentStatus.FAIL || payment.getStatus() == PaymentStatus.CANCELED) {
            throw new BadRequestException(FailureCode.INVALID_CONFIRM);
        }

        // PG 대상/금액 검증
        if (payment.getPgAmount() == null || payment.getPgAmount().signum() <= 0) {
            throw new BadRequestException(FailureCode.INVALID_CONFIRM);
        }
        if (payment.getPgAmount().compareTo(req.amount()) != 0) {
            throw new BadRequestException(FailureCode.AMOUNT_MISMATCH);
        }

        return null;
    }

    /**
     * 토스 성공 시 DB 반영 (트랜잭션)
     */
    @Transactional
    public ConfirmResultResponseDto applySuccess(String orderId, String paymentKey) {
        Payment payment = paymentRepository.findWithLockByTossOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.PAYMENT_NOT_FOUND));

        // 멱등성: 이미 DONE이면 성공 응답
        if (payment.getStatus() == PaymentStatus.DONE) {
            return ConfirmResultResponseDto.success(PaymentMapper.toCompletedDto(payment));
        }

        // PG 금액 충전 후 즉시 홀딩
        applyPgTopUpThenHold(payment);

        payment.setPaymentKeyIfAbsent(paymentKey);
        payment.markAsDone();

        eventPublisher.publishEvent(new PaymentCompletedEvent(
                payment.getRelType(), payment.getRelId(), payment.getTotalAmount()
        ));

        log.info("[PAYMENT_DONE] orderId={}, paymentKey={}, totalAmount={}",
                orderId, paymentKey, payment.getTotalAmount());

        return ConfirmResultResponseDto.success(PaymentMapper.toCompletedDto(payment));
    }

    /**
     * 토스 실패 시 DB 반영 (트랜잭션)
     */
    @Transactional
    public ConfirmResultResponseDto applyFailure(String orderId, String errorCode, String failReason) {
        Payment payment = paymentRepository.findWithLockByTossOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.PAYMENT_NOT_FOUND));

        // 멱등성: 이미 처리된 상태면 그에 맞게 응답
        if (payment.getStatus() == PaymentStatus.DONE) {
            return ConfirmResultResponseDto.success(PaymentMapper.toCompletedDto(payment));
        }
        if (payment.getStatus() == PaymentStatus.FAIL) {
            return ConfirmResultResponseDto.fail(PaymentMapper.toFailedDto(payment), errorCode, failReason);
        }

        handleConfirmFail(payment);

        eventPublisher.publishEvent(new PaymentFailedEvent(
                payment.getRelType(), payment.getRelId()
        ));

        return ConfirmResultResponseDto.fail(PaymentMapper.toFailedDto(payment), errorCode, failReason);
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

        BigDecimal held = payment.getWalletUsedAmount();
        if (held == null || held.signum() <= 0) return;

        if (payment.isReleased()) return;

        Wallet buyerWallet = walletSupport.getUserWallet(payment.getUserId());
        Wallet systemWallet = walletSupport.getSystemWallet();

        // 홀딩 되돌리기: 시스템 -held / 유저 +held
        systemWallet.withdraw(held);
        buyerWallet.deposit(held);

        cashLogSupport.recordReleaseOnPaymentFail(buyerWallet, systemWallet, held, payment.getRelType(), payment.getRelId());

        payment.markReleased();

        log.info("[PAYMENT_FAIL_RELEASED] orderId={}, releasedAmount={}",
                payment.getTossOrderId(), held);
    }
}
