package com.back.common.feign.cash.request;

import com.back.common.feign.cash.enums.RelType;

import java.math.BigDecimal;

public record PaymentCancelRequestDto(
        Long userId,        // 취소 요청자 (검증용, cash모듈에서)
        RelType relType,    // BIDDING or ORDER
        Long relId,         // 대상 ID
        BigDecimal amount  // 환불할 금액 (검증용, cash모듈에서)
) {
    public static PaymentCancelRequestDto of(Long userId, RelType relType, Long relId, BigDecimal amount) {
        return new PaymentCancelRequestDto(userId, relType, relId, amount);
    }
}
