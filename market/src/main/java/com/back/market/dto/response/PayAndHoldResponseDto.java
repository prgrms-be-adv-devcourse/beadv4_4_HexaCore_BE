package com.back.market.dto.response;

import com.back.market.dto.enums.PayAndHoldStatus;
import com.back.market.dto.enums.RelType;

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
