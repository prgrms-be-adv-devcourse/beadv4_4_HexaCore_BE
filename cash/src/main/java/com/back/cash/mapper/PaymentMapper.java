package com.back.cash.mapper;

import com.back.cash.domain.Payment;
import com.back.cash.domain.enums.PaymentStatus;
import com.back.common.dto.cash.enums.PayAndHoldStatus;
import com.back.common.dto.cash.request.PayAndHoldRequestDto;
import com.back.common.dto.cash.response.PayAndHoldResponseDto;

import java.math.BigDecimal;

public class  PaymentMapper {

    public static Payment toPayment(PayAndHoldRequestDto dto) {
        return Payment.builder()
                .userId(dto.buyerId())
                .relType(dto.relType())
                .relId(dto.relId())
                .totalAmount(dto.totalAmount())
                .walletUsedAmount(BigDecimal.ZERO)
                .pgAmount(BigDecimal.ZERO)
                .status(PaymentStatus.READY)
                .build();
    }

    /**
     * 예치금만으로 결제 완료(PAID) 응답
     */
    public static PayAndHoldResponseDto toPaidResponseDto(Payment payment) {
        return new PayAndHoldResponseDto(
                PayAndHoldStatus.PAID,
                payment.getRelType(),
                payment.getRelId(),
                payment.getWalletUsedAmount(),
                BigDecimal.ZERO,
                null
        );
    }

    /**
     * PG 필요(REQUIRES_PG) 응답
     */
    public static PayAndHoldResponseDto toRequiresPgResponseDto(Payment payment) {
        return new PayAndHoldResponseDto(
                PayAndHoldStatus.REQUIRES_PG,
                payment.getRelType(),
                payment.getRelId(),
                payment.getWalletUsedAmount(),
                payment.getPgAmount(),
                payment.getTossOrderId()
        );
    }

}
