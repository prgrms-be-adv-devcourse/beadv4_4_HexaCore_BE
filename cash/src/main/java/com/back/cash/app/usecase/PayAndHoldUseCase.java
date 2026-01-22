package com.back.cash.app;

import com.back.cash.adapter.out.PaymentRepository;
import com.back.cash.domain.Payment;
import com.back.cash.domain.Wallet;
import com.back.cash.domain.enums.PaymentStatus;
import com.back.cash.dto.request.PayAndHoldRequestDto;
import com.back.cash.dto.response.PayAndHoldResponseDto;
import com.back.cash.mapper.PaymentMapper;
import com.back.common.code.FailureCode;
import com.back.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PayAndHoldUseCase {
    private final WalletSupport walletSupport;
    private final CashLogSupport cashLogSupport;
    private final PaymentRepository paymentRepository;

    public PayAndHoldResponseDto execute(PayAndHoldRequestDto dto) {

        Payment payment = paymentRepository.findByRelTypeAndRelId(dto.relType(), dto.relId())
                .orElseGet(() -> paymentRepository.save(PaymentMapper.toPayment(dto)));

        // 재호출일 경우 totalAmount가 다르면 에러 발생
        if (payment.getTotalAmount().compareTo(dto.totalAmount()) != 0) {
            throw new BadRequestException(FailureCode.AMOUNT_MISMATCH);
        }

        // 이미 완료된 결제면 PAID 응답
        if (payment.getStatus() == PaymentStatus.DONE) {
            return PaymentMapper.toPaidResponseDto(payment);
        }

        // 이미 PG 결제 필요 상태면 REQUIRES_PG 응답 반환 
        if (isRequiresPgState(payment)) {
            return PaymentMapper.toRequiresPgResponseDto(payment);
        }

        Wallet buyerWallet = walletSupport.getUserWallet(dto.buyerId());
        Wallet systemWallet = walletSupport.getSystemWallet();

        BigDecimal total = payment.getTotalAmount();
        BigDecimal balance = buyerWallet.getBalance();

        BigDecimal walletUse = balance.min(total);
        BigDecimal pgNeed = total.subtract(walletUse);

        // 예치금 홀딩(가능한 만큼)
        if (walletUse.signum() > 0) {
            buyerWallet.withdraw(walletUse);
            systemWallet.deposit(walletUse);

            cashLogSupport.recordHoldingLog(
                    buyerWallet,
                    systemWallet,
                    walletUse,
                    payment.getRelType(),
                    payment.getRelId()
            );
        }

        payment.updateAmounts(walletUse, pgNeed);

        // PG 필요 없으면 즉시 완료
        if (pgNeed.signum() == 0) {
            payment.markAsDone();

            return PaymentMapper.toPaidResponseDto(payment);
        }

        // PG 필요하면 tossOrderId 발급 후 반환
        payment.issueTossOrderIdIfAbsent(java.util.UUID.randomUUID().toString());

        return PaymentMapper.toRequiresPgResponseDto(payment);
    }

    private boolean isRequiresPgState(Payment payment) {
        return payment.getStatus() != PaymentStatus.DONE
                && payment.getPgAmount() != null
                && payment.getPgAmount().signum() > 0
                && payment.getTossOrderId() != null;
    }
}


