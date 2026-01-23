package com.back.market.dto.response;

import com.back.common.dto.cash.enums.PayAndHoldStatus;
import com.back.common.dto.cash.enums.RelType;
import com.back.common.dto.cash.response.PayAndHoldResponseDto;

import java.math.BigDecimal;

public record MarketPaymentResponseDto(
        PayAndHoldStatus status,
        RelType relType,
        Long relId,
        BigDecimal walletUsedAmount,
        BigDecimal pgRequiredAmount,
        String tossOrderId,
        String orderName,
        String customerName,
        String customerEmail
) {
    // 편의 메서드: Cash 응답 + Market 정보 -> 최종 응답 변환
    public static MarketPaymentResponseDto from(
            PayAndHoldResponseDto cashDto,
            String orderName,
            String customerName,
            String customerEmail
    ) {
        return new MarketPaymentResponseDto(
                cashDto.status(),
                cashDto.relType(),
                cashDto.relId(),
                cashDto.walletUsedAmount(),
                cashDto.pgRequiredAmount(),
                cashDto.tossOrderId(),
                orderName,
                customerName,
                customerEmail
        );
    }
}
