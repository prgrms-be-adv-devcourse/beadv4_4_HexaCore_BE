package com.back.cash.app.usecase;

import com.back.cash.adapter.out.PaymentRepository;
import com.back.cash.app.CashLogSupport;
import com.back.cash.app.WalletSupport;
import com.back.cash.domain.Payment;
import com.back.cash.domain.Wallet;
import com.back.cash.domain.enums.PaymentStatus;
import com.back.cash.domain.event.PaymentFailedEvent;
import com.back.cash.dto.request.TossFailRequestDto;
import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import com.back.common.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FailTossPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final WalletSupport walletSupport;
    private final CashLogSupport cashLogSupport;

    public void execute(TossFailRequestDto req) {
        if (req == null || req.orderId() == null || req.orderId().isBlank()) {
            throw new BadRequestException(FailureCode.BAD_REQUEST);
        }

        Payment payment = paymentRepository.findWithLockByTossOrderId(req.orderId())
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.READY) {
            return;
        }

        payment.markAsFail();

        // 선홀딩이 있었다면 release
        releaseIfHeld(payment);

        eventPublisher.publishEvent(new PaymentFailedEvent(payment.getRelType(), payment.getRelId(), req.message()));
    }

    private void releaseIfHeld(Payment payment) {
        BigDecimal held = payment.getWalletUsedAmount();
        if (held == null || held.signum() <= 0) return;

        if (payment.isReleased()) return;

        Wallet buyerWallet = walletSupport.getUserWallet(payment.getUserId());
        Wallet systemWallet = walletSupport.getSystemWallet();

        systemWallet.withdraw(held);
        buyerWallet.deposit(held);

        cashLogSupport.recordReleaseOnPaymentFail(
                buyerWallet, systemWallet, held,
                payment.getRelType(), payment.getRelId()
        );

        payment.markReleased();
    }
}

