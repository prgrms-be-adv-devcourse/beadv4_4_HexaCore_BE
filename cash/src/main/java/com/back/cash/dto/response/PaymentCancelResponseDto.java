package com.back.cash.dto.response;

import java.math.BigDecimal;

public record PaymentCancelResponseDto(
        Long userId,
        BigDecimal refundedAmount
) {
    public static PaymentCancelResponseDto of(Long userId, BigDecimal refundedAmount) {
        return new PaymentCancelResponseDto(userId, refundedAmount);
    }
}
