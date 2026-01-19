package com.back.market.dto.request;

import com.back.market.dto.enums.RelType;

import java.math.BigDecimal;

public record PayAndHoldRequestDto (
        Long buyerId,
        BigDecimal totalAmount,
        String orderName,
        RelType relType,
        Long relId
) {
    public static PayAndHoldRequestDto of(Long buyerId, BigDecimal totalAmount, String orderName, RelType relType, Long relId) {
        return new PayAndHoldRequestDto(buyerId, totalAmount, orderName, relType, relId);
    }
}
