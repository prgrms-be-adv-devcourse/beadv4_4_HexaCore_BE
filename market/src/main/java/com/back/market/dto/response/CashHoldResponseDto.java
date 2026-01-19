package com.back.market.dto.response;

import java.math.BigDecimal;

public record CashHoldResponseDto(
        Long userId,
        BigDecimal holdAmount
) {
}
