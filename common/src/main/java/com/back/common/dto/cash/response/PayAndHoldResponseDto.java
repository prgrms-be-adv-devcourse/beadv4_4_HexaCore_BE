package com.back.common.dto.cash.response;

import com.back.common.dto.cash.enums.PayAndHoldStatus;
import com.back.common.dto.cash.enums.RelType;

import java.math.BigDecimal;

public record PayAndHoldResponseDto(
        PayAndHoldStatus status,
        RelType relType,
        Long relId,
        BigDecimal walletUsedAmount,
        BigDecimal pgRequiredAmount,
        String tossOrderId
) {
    public static PayAndHoldResponseDto of(
            PayAndHoldStatus status,
            RelType relType,
            Long relId,
            BigDecimal walletUsedAmount,
            BigDecimal pgRequiredAmount,
            String tossOrderId) {
        return new PayAndHoldResponseDto(status, relType, relId, walletUsedAmount, pgRequiredAmount, tossOrderId);
    }
}
