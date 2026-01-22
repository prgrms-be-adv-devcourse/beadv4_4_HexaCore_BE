package com.back.cash.dto.request;

import com.back.cash.domain.enums.RelType;

import java.math.BigDecimal;

public record PaymentCancelRequestDto(
        Long userId,
        RelType relType,
        Long relId,
        BigDecimal amount
) {
    public static PaymentCancelRequestDto of(Long userId, RelType relType, Long relId, BigDecimal amount) {
        return new PaymentCancelRequestDto(userId, relType, relId, amount);
    }
}
