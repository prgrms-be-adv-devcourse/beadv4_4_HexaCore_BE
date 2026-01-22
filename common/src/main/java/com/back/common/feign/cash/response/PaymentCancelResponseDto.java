package com.back.common.feign.cash.response;

import java.math.BigDecimal;

public record PaymentCancelResponseDto(
        Long userId,
        BigDecimal refundedAmount // 환불된 금액
) {
    public static PaymentCancelResponseDto of(Long userId, BigDecimal refundedAmount) {
        return new PaymentCancelResponseDto(userId, refundedAmount);
    }
}
