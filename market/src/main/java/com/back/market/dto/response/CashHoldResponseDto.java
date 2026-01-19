package com.back.market.dto.response;

import java.math.BigDecimal;

public record CashHoldResponseDto(
        Long userId,
        BigDecimal holdAmount
) {
    public static CashHoldResponseDto of(Long userId, BigDecimal holdAmount) {
        return new CashHoldResponseDto(userId, holdAmount);
    }
}
