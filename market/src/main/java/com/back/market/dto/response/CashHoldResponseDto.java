package com.back.market.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashHoldResponseDto {
    private Long userId;
    private BigDecimal holdAmount;   // 묶인 금액
}
