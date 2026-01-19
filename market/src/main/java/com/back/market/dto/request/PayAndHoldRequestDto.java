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
}
